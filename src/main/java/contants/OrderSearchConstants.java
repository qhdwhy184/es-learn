package contants;

public final class OrderSearchConstants {

    private OrderSearchConstants() {
        // 禁止实例
    }

    /**
     * es的最多的In操作的条件数量，不得大于1024个，所以如果有超过的，直接报错
     */
    public static final int MAX_CONTAIN_SIZE = 1000;

    public static final int NOT_SET = 0;
}
