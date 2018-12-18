package ru.miit.scud_service;

import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;
import java.util.logging.Level;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import weblogic.jws.Policy;
import weblogic.jws.Policies;
import weblogic.jws.security.RolesAllowed;
import weblogic.jws.security.SecurityRole;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;


@WebService

public class WebServiceMethods {

    //URL адрес web-сервиса
    private static final String NAME_SPACE_URL = "http://10.242.101.65:7101";


    //Метод возвращающий XML, содержащий данные о картах
    @WebMethod
    public String getCardsXML(@WebParam(name = "A_checkpoint_ID") long checkpointID,
                              @WebParam(name = "A_include_empl_info") int includeEmplInfo) {
        //Инициализация параметров
        String dataXMLReturn = null;

        //Проверка includeEmplInfo на соответствие 0, 1
        if (includeEmplInfo == 0 || includeEmplInfo == 1) {

            try {
                Core core = new Core();
                //Переаод includeEmplInfo в boolean
                boolean doEmpInfo = includeEmplInfo != 0;
                //Вызываем метод getCardsXMLFromDB и передаем входные параметры
                dataXMLReturn = core.getCardsXMLFromDB(checkpointID, doEmpInfo);
            } catch (ScudException e) {
                //Выбрасываем клиенту soapFault с кодом и описанием ошибки
                throwErrorCode(e.getErrorCode());
            }
        }

        else {
            //Выбрасываем клиенту soapFault, если includeEmplInfo не 1 или 0
            throwErrorCode(ru.miit
                             .scud_service
                             .ErrorCode
                             .ERROR_INCORRECT_PARAMETERS
                             .getValue());
        }

        return dataXMLReturn;
    }


    //Метод возвращающий XML, содержащий данные о карте из БД
    @WebMethod
    public String getCardInfoXML(@WebParam(name = "A_UID_Mifare") String mifareUID) {
        //Инициализация параметров
        String dataXMLReturn = null;

        Core core = new Core();

        //Проверка MifareID на соответствие паттерна
        if (core.checkMifareID(mifareUID)) {
            try {
                //Вызываем метод getCardXMLFromDB и передаем входные параметры
                dataXMLReturn = core.getCardXMLFromDB(mifareUID);

            } catch (ScudException e) {
                //Выбрасываем клиенту soapFault с кодом и описанием ошибки
                throwErrorCode(e.getErrorCode());
            }

        } else {
            //Выбрасываем клиенту soapFault, если MifareID не соответствует паттерну
            throwErrorCode(ru.miit
                             .scud_service
                             .ErrorCode
                             .ERROR_INCORRECT_PARAMETERS_MIFARE
                             .getValue());
        }

        return dataXMLReturn;
    }


    //Метод возвращающий XML с результатами обработки
    @WebMethod
    public String setEventsXML(@WebParam(name = "A_xml") String dataXMLInput,
                               @WebParam(name = "A_return_detail") int returnDetail) {
        //Инициализация параметров
        String dataXMLReturn = null;

        try {
            //Проверка returnDetail на соответствие 0, 1
            if (returnDetail == 0 || returnDetail == 1) {
                Core core = new Core();

                //Переаод doReturnDetail в boolean
                boolean doReturnDetail = returnDetail != 0;

                //Вызываем метод getEventsResponseXMLFromDB и передаем входные параметры
                dataXMLReturn = core.getEventsResponseXMLFromDB(dataXMLInput, doReturnDetail);
            } else
                //Выбрасываем клиенту soapFault, если returnDetail не 0 или 1
                throwErrorCode(ru.miit
                                 .scud_service
                                 .ErrorCode
                                 .ERROR_INCORRECT_PARAMETERS
                                 .getValue());

        } catch (ScudException e) {
            //Выбрасываем клиенту soapFault с кодом и описанием ошибки
            throwErrorCode(e.getErrorCode());
        }

        return dataXMLReturn;

    }

    //Метод возвращающий XML с результатом обработки
    @WebMethod
    public String setCardEventXML(@WebParam(name = "A_xml") String dataXMLInput) {
        //Инициализация параметров
        String dataXMLReturn = null;

        try {
            Core core = new Core();
            //Вызываем метод getEventResponseXMLFromDB и передаем входной XML файл
            dataXMLReturn = core.getEventResponseXMLFromDB(dataXMLInput);

        } catch (ScudException e) {
            //Выбрасываем клиенту soapFault с кодом и описанием ошибки
            throwErrorCode(e.getErrorCode());
        }

        return dataXMLReturn;

    }

    //Метод выбрасывающий soapFault клиенту
    public void throwErrorCode(int faultCodeValue) {

        //Инициализация параметров
        QName faultCode = new QName(NAME_SPACE_URL, String.valueOf(faultCodeValue));
        SOAPFault soapFault = null;
        //Получение комментария ошибки
        String faultString = getFaultString(faultCodeValue);

        try {
            //Создание soapFault с кодом и описанием ошибки
            soapFault = SOAPFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL).createFault(faultString, faultCode);
            throw new SOAPFaultException(soapFault);

        } catch (SOAPException e) {

            Core core = new Core();
            //Логирование исключений
            core.log.log(Level.SEVERE, "SOAPException: ", e);
        }

    }

    //Метод выбрасывающий soapFault клиенту
    public String getFaultString(int faultCodeValue) {
        InputStream propertiesIS = null;
        Core core = new Core();
        //Открываем поток к файлу, имеющему путь Resources/faultsName.properties
        try {
            propertiesIS =
                WebServiceMethods.class.getClassLoader().getResourceAsStream("Resources/faultsName.properties");
            Properties properties = new Properties();
            properties.load(propertiesIS);
            return properties.getProperty("errorCode" + faultCodeValue);

        } catch (Exception e) {


            //Логирование исключений
            core.log.log(Level.SEVERE, "Exception: ", e);
            return null;

        } finally {
            if (propertiesIS != null) {
                try {
                    propertiesIS.close();
                } catch (IOException e) {
                    core.log.log(Level.SEVERE, "OIException: ", e);
                }
            }

        }
    }


}
