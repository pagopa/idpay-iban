package it.gov.pagopa.iban.constants;

public class IbanConstants {
  public static final String UNKNOWN_PSP="UNKNOWN_PSP";
  public static final String KO="KO";
  public static final String OK="OK";
  public static final String ERROR_MSG_HEADER_SRC_TYPE = "srcType";
  public static final String ERROR_MSG_HEADER_SRC_SERVER = "srcServer";
  public static final String ERROR_MSG_HEADER_SRC_TOPIC = "srcTopic";
  public static final String ERROR_MSG_HEADER_DESCRIPTION = "description";
  public static final String ERROR_MSG_HEADER_RETRYABLE = "retryable";
  public static final String ERROR_MSG_HEADER_STACKTRACE = "stacktrace";
  public static final String ERROR_MSG_HEADER_CLASS = "rootCauseClass";
  public static final String ERROR_MSG_HEADER_MESSAGE = "rootCauseMessage";
  public static final String KAFKA = "kafka";
  public static final String ERROR_IBAN = "Error sending event to queue";
  public static final String CHANNEL_IO = "APP_IO";
  public static final String ISSUER_NO_CHECKIBAN = "ISSUER_NO_CHECKIBAN";

  public static final class ExceptionCode {
    public static final String IBAN_NOT_FOUND = "IBAN_NOT_FOUND";
    public static final String GENERIC_ERROR = "IBAN_GENERIC_ERROR";
    public static final String TOO_MANY_REQUESTS = "IBAN_TOO_MANY_REQUESTS";
    public static final String INVALID_REQUEST = "IBAN_INVALID_REQUEST";

    private ExceptionCode() {}
  }

  public static final class ExceptionMessage {
    public static final String IBAN_NOT_FOUND_MSG = "Iban not found for the current user";
    public static final String CHECKIBAN_INVOCATION_ERROR_MSG = "An error occurred in the checkiban invocation";

    private ExceptionMessage() {}
  }

  private IbanConstants(){

  }
}
