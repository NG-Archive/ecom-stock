package site.ng_archive.ecom_stock.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DocUtils {
    public static String enumFormat(Class<? extends Enum<?>> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.joining(", ", "(", ")"));
    }
}
