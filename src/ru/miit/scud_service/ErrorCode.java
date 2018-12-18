package ru.miit.scud_service;

public enum ErrorCode {
    /*
    ERROR_INCORRECT_PARAMETERS(1) - Входные параметры не соответствуют формату
    ERROR_XML_NOT_MATCH_XSD(2) - XML файл не соотвествует XSD
    ERROR_INCORRECT_PARAMETERS_MIFARE(3) - UID Mifare не соответствует паттерну (((\\d){1,17})|(0x[\\dA-F]{8,14}))
    ERROR_WITH_GET_DATA(4) - Ошибка при получении данных
    ERROR_INPUT_XML_NOT_MATCH_XSD(5) - Входной XML файл не соотвествует XSD
    */

    ERROR_INCORRECT_PARAMETERS(1),
    ERROR_XML_NOT_MATCH_XSD(2),
    ERROR_INCORRECT_PARAMETERS_MIFARE(3),
    ERROR_WITH_GET_DATA(4),
    ERROR_INPUT_XML_NOT_MATCH_XSD(5);

    private final int errorCode;

    private ErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getValue() {
        return errorCode;
    }

}
