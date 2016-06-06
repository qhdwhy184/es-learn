package param;

import java.util.Set;

/**
 * Created by wangyuanhui on 16/6/3.
 */
public class OrderSearchParam {
    private int limit;
    private int offset;
    /**
     * 排序字段字段
     */
    private String sortFieldName;
    private int userId;
    private long orderId;
    private Set<Long> orderIdSet;

    public String getSortFieldName() {
        return sortFieldName;
    }

    public void setSortFieldName(String sortFieldName) {
        this.sortFieldName = sortFieldName;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public Set<Long> getOrderIdSet() {
        return orderIdSet;
    }

    public void setOrderIdSet(Set<Long> orderIdSet) {
        this.orderIdSet = orderIdSet;
    }
}
