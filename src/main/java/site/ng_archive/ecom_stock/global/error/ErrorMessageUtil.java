package site.ng_archive.ecom_stock.global.error;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ErrorMessageUtil {

    private final MessageSource ms;
    private static final String EXCEPTION_ERROR_CODE = "error";

    public String getErrorCode(Exception e) {
        String errorCode = e.getMessage();
        try {
            ms.getMessage(errorCode, null, Locale.KOREA);
        } catch (Exception ex) {
            return EXCEPTION_ERROR_CODE;
        }
        return errorCode;
    }

    public String getErrorMessage(String errorCode) {
        try {
            return ms.getMessage(errorCode, null, Locale.KOREA);
        } catch (Exception ex) {
            return ms.getMessage(EXCEPTION_ERROR_CODE, null, Locale.KOREA);
        }
    }

    public String getErrorMessage(String errorCode, Object[] args) {
        Object[] reversed = null;

        if (args != null && args.length > 1) {
            Object[] copied = Arrays.copyOfRange(args, 1, args.length);
            reversed = Arrays.asList(copied).reversed().toArray();
        }
        try {
            return ms.getMessage(errorCode, reversed, Locale.KOREA);
        } catch (Exception ex) {
            return ms.getMessage(EXCEPTION_ERROR_CODE, null, Locale.KOREA);
        }
    }

    public ErrorResponse getErrorResult(String errorCode, String message) {
        return new ErrorResponse(errorCode, message);
    }

    public ErrorResponse getErrorResult(Exception e) {
        String code = getErrorCode(e);
        String message = getErrorMessage(code);
        return new ErrorResponse(code, message);
    }

}
