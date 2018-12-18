package ru.miit.scud_service;


import java.io.IOException;

import javax.sql.DataSource;

import java.io.InputStream;
import java.io.StringReader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import javax.naming.InitialContext;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;


public class Core {

    //Создание экземпляра класса необходимого для логирования
    public Logger log = Logger.getLogger(Core.class.getName());

    //SQL запрос с параметрами
    private final String getCardsXMLFromDBSQL =
        "Select S.all AS cardsXML from cards_table S where :id_parameter_1 is not null and :id_parameter_2";

    private final String getCardXMLFromDBSQL =
        "Select S.all AS cardXML from card_table S where :id_parameter is not null";

    private final String getEventsResponseXMLFromDBSQL =
        "Select S.all AS eventsResponseXML from eventsResponse_table S where :id_parameter_1 is not null and :id_parameter_2";

    private final String getEventResponseXMLFromDBSQL =
        "Select S.all AS eventResponseXML from eventResponse_table S where :id_parameter is not null";

    //Метод возвращающий XML, содержащий данные о картах из БД
    public String getCardsXMLFromDB(long checkpointID, boolean emplInfoBool) throws ScudException {

        //Инициализвация переменных
        String cardsXML = null;

        cardsXML = getXMLFromDB(getCardsXMLFromDBSQL, "cardsXML");

        //Валидация полученного XML
        if (validateXMLByXSD(cardsXML)) {
            return cardsXML;
        } else {
            //Выбрасываем исключение с кодом ошибки
            throw new ScudException(ErrorCode.ERROR_XML_NOT_MATCH_XSD);
        }

    }


    //Метод возвращающий XML, содержащий данные о карте из БД
    public String getCardXMLFromDB(String mifareUID) throws ScudException {

        //Инициализвация переменных
        String cardXML = null;

        cardXML = getXMLFromDB(getCardXMLFromDBSQL, "cardXML");

        //Валидация полученного XML
        if (validateXMLByXSD(cardXML))
            return cardXML;
        else
            //Выбрасываем исключение с кодом ошибки
            throw new ScudException(ErrorCode.ERROR_XML_NOT_MATCH_XSD);

    }

    //Метод возвращающий XML с результатами обработки
    public String getEventsResponseXMLFromDB(String eventsXML, boolean detailBool) throws ScudException {
        String eventsResponseXML = null;

        //Валидация входного XML
        if (validateXMLByXSD(eventsXML)) {

            //Инициализвация переменных

            eventsResponseXML = getXMLFromDB(getEventsResponseXMLFromDBSQL, "eventsResponseXML");

            //Валидация полученного XML
            if (validateXMLByXSD(eventsResponseXML))
                return eventsResponseXML;
            else
                //Выбрасываем исключение с кодом ошибки
                throw new ScudException(ErrorCode.ERROR_XML_NOT_MATCH_XSD);
        } else
            //Выбрасываем исключение с кодом ошибки
            throw new ScudException(ErrorCode.ERROR_INPUT_XML_NOT_MATCH_XSD);
    }


    //Метод возвращающий XML с результатом обработки
    public String getEventResponseXMLFromDB(String eventXML) throws ScudException {
        String eventResponseXML = null;

        //Валидация входного XML
        if (validateXMLByXSD(eventXML)) {


            eventResponseXML = getXMLFromDB(getEventResponseXMLFromDBSQL, "eventResponseXML");


            //Валидация полученного XML
            if (validateXMLByXSD(eventResponseXML))
                return eventResponseXML;
            else
                //Выбрасываем исключение с кодом ошибки
                throw new ScudException(ErrorCode.ERROR_XML_NOT_MATCH_XSD);
        } else
            //Выбрасываем исключение с кодом ошибки
            throw new ScudException(ErrorCode.ERROR_INPUT_XML_NOT_MATCH_XSD);
    }

    //Метод возвращающий XML, содержащий данные о карте из БД
    public String getXMLFromDB(String requestSQL, String resultName) throws ScudException {
        String returntXML = null;
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {

            //Cоздания нового  контекста
            InitialContext initialContext = new InitialContext();
            DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/jdbc/ds_basic");

            //Открываем соединение
            connection = dataSource.getConnection();
            statement = connection.createStatement();


            //Выполнение запроса
            resultSet = statement.executeQuery(requestSQL);

            //Получаем данные с  resultset
            resultSet.next();
            returntXML = resultSet.getString(resultName);

            return returntXML;

        } catch (SQLException e) {
            //Обработка ошибкок JDBC и запись в лог
            log.log(Level.SEVERE, "SQLException: ", e);
            throw new ScudException(ErrorCode.ERROR_WITH_GET_DATA);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception: ", e);
            //Выбрасываем исключение с кодом ошибки
            throw new ScudException(ErrorCode.ERROR_WITH_GET_DATA);
        } finally {
            if (resultSet != null) {

                try {
                    resultSet.close();
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "SQLException: ", e);
                }
            }

            if (statement != null) {

                try {
                    statement.close();
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "SQLException: ", e);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "SQLException: ", e);
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


