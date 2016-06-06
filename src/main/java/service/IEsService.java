package service;

import model.OrderIndexModel;
import param.OrderSearchParam;

import java.util.List;

/**
 * Created by wangyuanhui on 16/6/3.
 */
public interface IEsService {
    /**
     * 批量存储
     * @param orderIndexModels
     * @return
     */
    boolean saveOrderIndex(List<OrderIndexModel> orderIndexModels);

    /**
     * 根据条件,返回OrderId
     */
    List<Long> queryId(OrderSearchParam searchParam);
}
