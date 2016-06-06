package service;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import junit.framework.Assert;
import model.OrderIndexModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import param.OrderSearchParam;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wangyuanhui on 16/6/3.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = true)
@ContextConfiguration(locations = {"classpath*:spring/spring-config.xml"})
public class EsServiceImplTest {
    @Resource
    private IEsService esService;

    @Test
    public void testCreateIndex() {
        OrderIndexModel orderIndexModel = new OrderIndexModel();
        orderIndexModel.setOrderId(133);
        orderIndexModel.setUserId(1330);
        OrderIndexModel orderIndexModel2 = new OrderIndexModel();
        orderIndexModel2.setOrderId(444);
        orderIndexModel2.setUserId(4440);

        // 1.存
        boolean saveRes = esService.saveOrderIndex(Arrays.asList(orderIndexModel,orderIndexModel2));
        Assert.assertTrue(saveRes);

        System.out.println("--------result before---------");
    }

    @Test
    public void testQuery() {
        try{
            OrderSearchParam param = new OrderSearchParam();
            param.setSortFieldName("ASC");
            List<Long> ids = esService.queryId(param);
            System.out.println(new Gson().toJson(ids));
        }catch (Exception e) {
            System.out.println(new Gson().toJson(e));
        }
        System.out.println("--------result before---------");
    }
}
