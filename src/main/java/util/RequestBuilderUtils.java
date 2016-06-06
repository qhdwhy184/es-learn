package util;

import com.google.common.base.Strings;
import contants.OrderSearchConstants;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Set;

/**
 * request builder utils
 */
public class RequestBuilderUtils {

    private RequestBuilderUtils() {
        // 禁止实例
    }

    /**
     * 构造返回特定字段接管的request builder
     *
     * @param builder
     * @param fields 需要返回的字段
     * @return
     */
    public static SearchRequestBuilder buildSpecifiedFieldBuilder(SearchRequestBuilder builder, Set<String> fields) {

        String[] fieldsArray = fields.toArray(new String[fields.size()]);
        builder.addFields(fieldsArray);

        return builder;
    }

    /**
     * 构建带排序的request builder
     *
     * @param builder
     * @param sortField
     * @param isAsc
     * @return
     */
    public static SearchRequestBuilder addSort(SearchRequestBuilder builder, String sortField, boolean isAsc) {

        if (Strings.isNullOrEmpty(sortField)) {
            return builder;
        }

        SortOrder sortOrder = isAsc ? SortOrder.ASC : SortOrder.DESC;
        builder.addSort(sortField, sortOrder);

        return builder;
    }

    /**
     * 设置builder的size from
     *
     * @param builder
     * @param offset
     * @param limit
     * @return
     */
    public static SearchRequestBuilder setFromAndSize(SearchRequestBuilder builder, int offset, int limit) {


        if (OrderSearchConstants.NOT_SET == limit) {
            return builder;
        }

        builder.setFrom(offset).setSize(limit);

        return builder;
    }

}
