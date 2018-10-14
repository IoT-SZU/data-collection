package cn.ac.futurenet.data_collection.utils;

/**
 * Created by chenlin on 29/03/2018.
 */
public class ArrayUtil {
    public static final String DEFAULT_SEPARATOR = ",";

    public static String join(float[] data) {
        return join(data, DEFAULT_SEPARATOR);
    }

    public static String join(float[] data, String separator) {
        if (data.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(data[0]);
        for (int i = 1; i < data.length; ++i) {
            sb.append(separator + data[i]);
        }
        return sb.toString();
    }

    public static String join(Float[] data) {
        return join(data, DEFAULT_SEPARATOR);
    }

    public static String join(Float[] data, String separator) {
        if (data.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(data[0]);
        for (int i = 1; i < data.length; ++i) {
            sb.append(separator + data[i]);
        }
        return sb.toString();
    }
}
