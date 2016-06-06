package util;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.*;
import param.OrderSearchParam;

import java.util.Set;

/**
 * query builder utils
 */
public class QueryBuilderUtils {

    private QueryBuilderUtils() {
        // 禁止实例
    }

    /**
     * 没设置值的默认值的常量
     */
    protected static final int NOT_SET = 0;

    /**
     * 根据orderSearchParam的条件构造queryBuilder
     *
     * @param param
     * @return
     */
    public static QueryBuilder getQueryBuilder(OrderSearchParam param) {

        //bool查询器
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//
//
        //orderId
        addQueryByMatch(boolQueryBuilder, "_id", param.getOrderId(), param.getOrderIdSet());
//
//

        return boolQueryBuilder;
    }

    /**
     * 增加等于或者in的查询条件
     *
     * @param boolQueryBuilder 布尔查询器
     * @param name             字段名称
     * @param value            要等于的值
     * @param setValue         要in的值
     * @param <T>
     */
    public static <T> void addQueryByMatch(BoolQueryBuilder boolQueryBuilder, String name, T value, Set<T> setValue) {

        //如果是数字，那么0就是说明没set过，非0的，说明需要加到筛选条件中
        if (value instanceof Number && ((Number) value).longValue() != NOT_SET) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(name, value));
        }
        //如果是字符串，那么非空说明需要加到筛选条件中
        if (value instanceof String && StringUtils.isNotEmpty((String) value)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(name, value));
        }
        //如果in的字段不为空，那么需要加到term查询器里面
        if (CollectionUtils.isNotEmpty(setValue)) {
            boolQueryBuilder.must(QueryBuilders.termsQuery(name, setValue));
        }
    }
}
