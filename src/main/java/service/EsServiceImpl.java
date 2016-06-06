package service;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import model.OrderIndexModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.stereotype.Service;
import param.OrderSearchParam;
import util.QueryBuilderUtils;
import util.RequestBuilderUtils;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangyuanhui on 16/6/3.
 */
@Service
public class EsServiceImpl implements IEsService {
    private static final String INDEX_CATEGORY = "order";
    private static final String ORDER_DETAIL = "orderDetail";
    private Client client = null;
    /**
     * 初始化的参数，ip列表
     */
    protected String serverIps = "192.168.1.1:9300";

    /**
     * 集群名称
     */
    protected String clusterName = "ticket-stat-offline";

    public static void main(String[] args) {
        try {
            Settings settings = Settings.settingsBuilder()
                    .put("cluster.name", "ticket-stat-offline").build();
            Client client = TransportClient.builder().settings(settings).build().
                    addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.1.1"), 9300));

        // XXX is my server's ip address

            IndexResponse response = client.prepareIndex("twitter", "tweet")
                    .setSource(XContentFactory.jsonBuilder()
                            .startObject()
                            .field("productId", "1")
                            .field("productName", "XXX").endObject()).execute().actionGet();
            response.getId();
        } catch (Exception e) {
            System.out.println(new Gson().toJson(e));
        }
    }

    @PostConstruct
    public void postConstruct() throws Exception {
        if(StringUtils.isEmpty(serverIps) || StringUtils.isEmpty(clusterName)){
            System.out.println("can not init es client!");
        }

        String[] servers = serverIps.split(",");

        List<InetSocketTransportAddress> serverList = Lists.newArrayList();
        for(String server : servers) {
            InetSocketTransportAddress address = getInetSocketTransportAddress(server);
            serverList.add(address);
        }
        client = init(serverList, clusterName);
    }

    public boolean saveOrderIndex(List<OrderIndexModel> orderIndexModels) {
        //批量操作接口
        BulkRequestBuilder bulkRequest = client.prepareBulk();

        for (OrderIndexModel orderIndexModel : orderIndexModels) {

            String jsonValue = JSON.toJSONString(orderIndexModel);

            IndexRequest indexRequest = client.prepareIndex(INDEX_CATEGORY, ORDER_DETAIL,
                    String.valueOf(orderIndexModel.getOrderId())).
                    setSource(jsonValue).request();

            bulkRequest.add(indexRequest);
        }

        Stopwatch started = Stopwatch.createStarted();

        BulkResponse bulkResponse = bulkRequest.execute().actionGet();

        System.out.println("bulk size: " + orderIndexModels.size() + ", cost " + started.elapsed(TimeUnit.MILLISECONDS) + "ms.");

        if (bulkResponse.hasFailures()) {
            String failReason = bulkResponse.buildFailureMessage();
            System.out.println("bulk operation error! " + failReason);
            return false;
        }

        return true;
    }

    public List<Long> queryId (OrderSearchParam searchParam){
        String shardTable = INDEX_CATEGORY;

        return queryIdBase(searchParam, shardTable);
    }

    private List<Long> queryIdBase(OrderSearchParam searchParam, String ... shardTable) {


        SearchRequestBuilder builder = client.prepareSearch(shardTable).setTypes(ORDER_DETAIL);
        builder.setSearchType(SearchType.DEFAULT);

        if(searchParam.getLimit() != 0){
            builder.setFrom(searchParam.getOffset()).setSize(searchParam.getLimit());
        }

        //设置查询条件
        QueryBuilder qb = QueryBuilderUtils.getQueryBuilder(searchParam);
        builder.setQuery(qb);

        return getOnlyIdSearchResult(builder);
    }

    /**
     * 把类似123.123.123.123:9300这样的字符串变成InetSocketTransportAddress返回
     */
    protected InetSocketTransportAddress getInetSocketTransportAddress(String server) throws UnknownHostException {
        String[] ipAndPort = server.split(":");

        if (ipAndPort.length != 2) {
            System.out.println("can not init es client!");
        }

        String ip = ipAndPort[0];
        String portStr = ipAndPort[1];
        if (StringUtils.isEmpty(portStr) || !StringUtils.isNumeric(portStr)) {
            System.out.println("can not init es client!");
        }
        return new InetSocketTransportAddress(InetAddress.getByName(ip), Integer.parseInt(portStr));
    }

    /**
     * 初始化client
     *
     * @param serverList
     * @param clusterName
     * @return
     */
    protected Client init(List<InetSocketTransportAddress> serverList, String clusterName) throws UnknownHostException {

        if (CollectionUtils.isEmpty(serverList) || StringUtils.isEmpty(clusterName)) {
            System.out.println("can not init es client!");
        }

        Settings settings = Settings.settingsBuilder()
                .put("client.transport.sniff", true).put("cluster.name", clusterName).build();
        Client client = TransportClient.builder().settings(settings).build();


        for (InetSocketTransportAddress address : serverList) {
            ((TransportClient) client).addTransportAddress(address);
        }
        return client;
    }

    /**
     * 指定返回字段，只返回特定字段的结果
     *
     * @param builder
     * @return
     */
    protected List<Long> getOnlyIdSearchResult(SearchRequestBuilder builder) {

        builder = RequestBuilderUtils.buildSpecifiedFieldBuilder(builder, Sets.newHashSet("_id"));

        List<Long> orderIds = getSearchResults(builder);

        return orderIds;
    }

    /**
     * 获取查询结果，返回的都是文档的具体id，也就是orderId
     *
     * @param builder
     * @return
     */
    protected List<Long> getSearchResults(SearchRequestBuilder builder) {

        Stopwatch stopwatch = Stopwatch.createStarted();

        SearchResponse response = builder.execute().actionGet();

        SearchHit[] res = response.getHits().getHits();
        if (ArrayUtils.isEmpty(res)) {
            return Lists.newArrayList();
        }

        List<Long> result = Lists.newArrayList();

        //不管哪一个索引，id都是orderId，直接把orderId返回
        for (SearchHit hit : res) {
            String idStr = hit.getId();
            if (StringUtils.isNumeric(idStr)) {
                result.add(Long.parseLong(idStr));
            }
        }

        return result;
    }
}