package ru.miit.scud_service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;


public class CoreTest {

    //Создание экземпляра класса необходимого для логирования
    public Logger log = Logger.getLogger(Core.class.getName());

    //Метод возвращающий XML, содержащий данные о картах из БД
    public String getCardsXMLFromDB(long checkpointID, boolean emplInfoBool) throws ScudException {

        //Инициализвация переменных
        String cardsXML = null;

        //Полоучение XML из properties
        cardsXML = getXMLFromProperties("cards");

        //Валидация полученного XML
        if (validateXMLByXSD(cardsXML))
            return cardsXML;
        else
            //Выбрасываем исключение с кодом ошибки
            throw new ScudException(ErrorCode.ERROR_XML_NOT_MATCH_XSD);


    }

    //Метод возвращающий XML, содержащий данные о карте из БД
    public String getCardXMLFromDB(String mifareUID) throws ScudException {

        //Инициализвация переменных
        String cardXML = null;

        //Полоучение XML из properties
        cardXML = getXMLFromProperties("card");

        //Валидация полученного XML
        if (validateXMLByXSD(cardXML))
            return cardXML;
        else
            //Выбрасываем исключение с кодом ошибки
            throw new ScudException(ErrorCode.ERROR_XML_NOT_MATCH_XSD);

    }

    //Метод возвращающий XML с результатами обработки
    public String getEventsResponseXMLFromDB(String eventsXML, boolean detailBool) throws ScudException {

        //Инициализвация переменных
        String eventsResponsXML = null;

        //Валидация входного XML
        if (validateXMLByXSD(eventsXML)) {

            //Полоучение XML из properties
            eventsResponsXML = getXMLFromProperties("eventsResponse");

            //Валидация полученного XML
            if (validateXMLByXSD(eventsResponsXML))
                return eventsResponsXML;
            else
                //Выбрасываем исключение с кодом ошибки
                throw new ScudException(ErrorCode.ERROR_XML_NOT_MATCH_XSD);
        } else
            //Выбрасываем исключение с кодом ошибки
            throw new ScudException(ErrorCode.ERROR_INPUT_XML_NOT_MATCH_XSD);
    }

    //Метод возвращающий XML с результатом обработки
    public String getEventResponseXMLFromDB(String eventXML) throws ScudException {

        //Инициализвация переменных
        String eventResponsXML = null;

        //Валидация входного XML
        if (validateXMLByXSD(eventXML)) {

            //Полоучение XML из properties
            eventResponsXML = getXMLFromProperties("eventResponse");

            //Валидация полученного XML
            if (validateXMLByXSD(eventResponsXML))
                return eventResponsXML;
            else
                //Выбрасываем исключение с кодом ошибки
                throw new ScudException(ErrorCode.ERROR_XML_NOT_MATCH_XSD);
        } else
            //Выбрасываем исключение с кодом ошибки
            throw new ScudException(ErrorCode.ERROR_INPUT_XML_NOT_MATCH_XSD);


    }

    //Метод получения XML с properties
    public String getXMLFromProperties(String nameProperties) throws ScudException {
        InputStream propertiesIS = null;
        //Открываем поток к файлу, имеющему путь ResourcesTest/testXML.properties
        try {

            propertiesIS = Core.class.getClassLoader().getResourceAsStream("ResourcesTest/testXML.properties");
            Properties properties = new Properties();
            properties.load(propertiesIS);
            return properties.getProperty(nameProperties);

        } catch (Exception e) {

            //Логирование исключений
            log.log(Level.SEVERE, "Exception: ", e);

            //Выбрасываем исключение с кодом ошибки
            throw new ScudException(ErrorCode.ERROR_WITH_GET_DATA);
        } finally {
            if (propertiesIS != null) {
                try {
                    propertiesIS.close();
                } catch (IOException e) {
                    log.log(Level.SEVERE, "OIException: ", e);
                }
            }

        }

    }

    //Метод, производящий валидацию XML файла на основе схемы XSD
    public boolean validateXMLByXSD(String validateXML) throws ScudException {

        InputStream schemaIS = null;

        //Открываем поток к схеме, имеющей путь XSDSchema/MiitPacs.xsd
        try {
            schemaIS = Core.class.getClassLoader().getResourceAsStream("XSDSchema/MiitPacs.xsd");
            //Валидация
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                         .newSchema(new StreamSource(schemaIS))
                         .newValidator()
                         .validate(new StreamSource(new StringReader(validateXML)));
            return true;

        } catch (Exception e) {
            //Логирование исключений
            log.log(Level.SEVERE, "Exception: ", e);
            return false;
        } finally {
            if (schemaIS != null) {
                try {
                    schemaIS.close();
                } catch (IOException e) {
                    log.log(Level.SEVERE, "OIException: ", e);
                }
            }

        }
    }

    //Метод, проверяющий mifareUID на соответсвие паттерну
    public boolean checkMifareID(String mifareUID) {

        //Паттерн
        String regexp = "(((\\d){1,17})|(0x[\\dA-F]{8,14}))";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(mifareUID);

        //Проверка на паттерн
        if (matcher.matches())
            return true;
        else
            return false;
    }
}


