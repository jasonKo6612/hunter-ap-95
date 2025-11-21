package com.e104;

/**
 * History： <br>
 * 2008/8/20 Sean.Chen Created<br>
 * 2008/11/12 Sean.chen modify BEP00-00001-1237 獵才派遣共用元件整合專案<br> 
 * 2012/09/10 sally.huang  modify db migration<br>
 * 2012/10/08 update by Josie Wu at BEP00-00001-1462 MySQL轉換作業專案<br>
 * 2012/12/19 Josie Wu modify BEP00-00001-1469 My104多重履歷改版配合<br>
 * 20180910 Peter Tsai HTHUNTERREQ-45 [獵才]職缺刊登同步主網改接JOB API
 * 20180926 Peter Tsai HTHUNTERREQ-62 [獵才]主網客戶及職缺資料搜尋改用即時index
 * 20230117 Peter Tsai HTHUNTERREQ-1237 [獵才]AP 版更
 */
/**
 * History：<br>
 * 2016/09/05 update by Peter.Tsai at 調整AP架構<br>
 */  
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SMTPAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
//import com.ht.util.XmlGlobalHandlerNewAP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import com.ht.util.XmlLocalHandlerNewAP;

/**
 * Description： 刊登超過60天時，自動將該職缺關閉後再開啟 <br>
 * Classname：AP95.java<br>
 * Date：2008/8/20<br>
 * Author：Sean Chen<br>
 * Copyright (c) 104hunter All Rights Reserved.<br>
 */
public class AP95 {
    private static final Logger logger = LoggerFactory.getLogger(AP95.class);
    private static Properties config = null;
    private static Connection con = null;
    private String nodeEnv = "";
//    private static XmlGlobalHandlerNewAP globalXML = null;
    //private static XmlLocalHandlerNewAP localXML = null;

    // Log
//    private static File fLogFile = null;
//    private static BufferedWriter bwLogFile = null;
//    private static PrintWriter pw = null;
//    private static SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy/MM/dd hh:mm:ss" );

    /**
     * <summary>查詢資料</summary><br>
     * @param
     * @return
     * @throws IOException
     */
    private void queryData() throws Exception {
//        Connection conHun = null;
        Statement stHun = null;
		PreparedStatement pstHun1 = null;
        PreparedStatement pstHun2 = null;
		PreparedStatement pstHun3 = null;
        PreparedStatement pstCaseWeb = null;
        //Connection conJobbank = null;
		//PreparedStatement pstJobbank = null;
        ResultSet rsHun = null;
        try {
            // 註冊、建立ORACLE Connection
//            Class.forName( globalXML.getGlobalTagValue( "dsn1.driver" ) );
            //Class.forName( globalXML.getGlobalTagValue( "dsn2.driver" ) );
//            conHun = DriverManager.getConnection( globalXML.getGlobalTagValue( "dsn1.database" ), globalXML.getGlobalTagValue( "dsn1.username" ), globalXML.getGlobalTagValue( "dsn1.password" ) );
            stHun = con.createStatement();
            pstHun1 = con.prepareStatement( "Update `CASE` set Web_Start_Date=date_format(now(),'%Y/%m/%d') where caid=?" );
            pstHun2 = con.prepareStatement( "Update `CASE_CN` set Web_Start_Date=date_format(now(),'%Y/%m/%d') where caid=?" );
			pstHun3 = con.prepareStatement( "Update `CASE_EN` set Web_Start_Date=date_format(now(),'%Y/%m/%d') where caid=?" );
			pstCaseWeb = con.prepareStatement( "update CASE_WEB set PROGRESS = 'U', PROGRESS_DATE = now() where caid = ? and ((ROLE = 1 and JOBNO_F > 0) or (ROLE = 2 and JOBNO_H > 0))" );
			//conJobbank = DriverManager.getConnection( globalXML.getGlobalTagValue( "dsn2.database" ), globalXML.getGlobalTagValue( "dsn2.username" ), globalXML.getGlobalTagValue( "dsn2.password" ) );
            //pstJobbank = conJobbank.prepareStatement( "Update jobon set firstdate=now() where custno='11111119000' and password=?" );

            String strSQL = "Select Caid,Web_Start_Date from `Case` Where Web_Status104=2 and round(datediff(now(),Web_Start_Date))>60 ";
			logger.info("刊登超過60天之職缺SQL：{}", strSQL);
			rsHun = stHun.executeQuery(strSQL);
			while( rsHun.next() ) {
                logger.info("Caid：{} , Web_Start_Date：{}", rsHun.getString("caid"), rsHun.getString("Web_Start_Date"));
                pstHun1.clearParameters();
                pstHun1.setString( 1, rsHun.getString( "caid" ) );
                pstHun1.executeUpdate();
				
				pstHun2.clearParameters();
                pstHun2.setString( 1, rsHun.getString( "caid" ) );
                pstHun2.executeUpdate();
				
				pstHun3.clearParameters();
                pstHun3.setString( 1, rsHun.getString( "caid" ) );
                pstHun3.executeUpdate();
                
				//HTHUNTERREQ-45 [獵才]職缺刊登同步主網改接JOB API，
				//更新CASE_WEB.PROGRESS 以啟動AP2185將職缺刊登更新同步主網改接JOB API
				//與主網JOBLIST排序相關的是appear_date，由API程式處理，超過七天會自動更新，而firstdate早就與排序無關，第一次寫入後不應再異動
				//為避免舊資料中有ROLE =3 的情況，會造成AP2185執行異常，故更新條件加上 and ((ROLE = 1 and JOBNO_F > 0) or (ROLE = 2 and JOBNO_H > 0))
				pstCaseWeb.clearParameters();
				pstCaseWeb.setString( 1, rsHun.getString( "caid" ) );
				pstCaseWeb.executeUpdate();
						
                //pstJobbank.clearParameters();
                //pstJobbank.setString( 1, rsHun.getString( "caid" ) );
                //pstJobbank.executeUpdate();
            }
        //} catch( ClassNotFoundException e ) {
        //    bwLogFile.write( "##不成功##\r\n" );
        //    bwLogFile.write( "queryData() exception :\r\n" );
        //    e.printStackTrace( pw );
		//} catch( SQLException e ) {
        //    bwLogFile.write( "##不成功##\r\n" );
        //    bwLogFile.write( "queryData() exception :\r\n" );
        //    e.printStackTrace( pw );
        } catch( Exception e ) {
//            logger.error("##不成功##", e);
            logger.error("queryData() exception", e);
        } finally {
            if( rsHun != null ) {
                try {
                    rsHun.close();
                } catch( SQLException e ) {
//                    logger.error("##不成功##", e);
                    logger.warn("queryData() exception - rsHun.close()", e);
                }
            }
            if( stHun != null ) {
                try {
                    stHun.close();
                } catch( SQLException e ) {
//                    logger.error("##不成功##", e);
                    logger.warn("queryData() exception - stHun.close()", e);
                }
            }
			if( pstHun1 != null ) {
                pstHun1.close();
            }
            if( pstHun2 != null ) {
                pstHun2.close();
            }
			if( pstHun3 != null ) {
                pstHun3.close();
            }
			if( pstCaseWeb != null ) {
        		pstCaseWeb.close();
            }

            if( con != null ) {
                try {
                    con.close();
                } catch( SQLException e ) {
//                    logger.error("##不成功##", e);
                    logger.warn("queryData() exception - conHun.close()", e);
                }
            }
			
			/*
			if( pstJobbank != null ) {
				try {
					pstJobbank.close();
				} catch( SQLException e ) {
					bwLogFile.write( "##不成功##\r\n" );
					bwLogFile.write( "queryData() exception :\r\n" );
					e.printStackTrace( pw );
				}
			}
			if( conJobbank != null ) {
				try {
					conJobbank.close();
				} catch( SQLException e ) {
					bwLogFile.write( "##不成功##\r\n" );
					bwLogFile.write( "queryData() exception :\r\n" );
					e.printStackTrace( pw );
				}
			}
			*/
        }
    }

    /**
     * 設定環境變數與讀取設定檔
     *
     * @throws IOException
     */
    private void setEnvProperties() throws IOException {
        nodeEnv = System.getenv("NODE_ENV");
        if (nodeEnv == null || nodeEnv.isEmpty()) {
            nodeEnv = "";
        }
        logger.info("nodeEnv = {}", nodeEnv);

        String filePath = "/opt/.hunter-env.properties";
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            properties.load(fis);
            logger.info("成功載入設定檔: {}", filePath);
        } catch (IOException e) {
            logger.error("無法讀取設定檔: {}", filePath, e);
            throw e;
        }
        config = properties;
    }

    /**
     * 建立資料庫連線
     *
     * @param account 屬性鍵值前綴
     * @return Connection object
     * @throws SQLException
     */
    private Connection createDatabaseConnection(String account) throws SQLException {
        logger.info("建立資料庫連線: {}", account);
        String host = config.getProperty(account + ".credential.host");
        String port = config.getProperty(account + ".credential.port");
        String database = config.getProperty(account + ".credential.dbClusterIdentifier");
        String url = String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf8&useSSL=false", host, port, database);
        logger.info("建立資料庫連線 URL: {}", url);
        String username = config.getProperty(account + ".credential.username");
        String password = config.getProperty(account + ".credential.password");
        logger.info("建立資料庫連線 username: {}", username);
        logger.info("建立資料庫連線 password: {}", (password != null ? new String(new char[password.length()]).replace("\0", "*") : "null"));

        return DriverManager.getConnection(url, username, password);
    }

    /**
     * 設定資料庫連線
     *
     * @throws Exception
     */
    private void setDataBaseConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        con = createDatabaseConnection("m008001_b0019_ai");
    }

    /**
     * 印出 Logback SMTP Appender 配置資訊
     */
    private void printLogbackSmtpConfig() {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            for (ch.qos.logback.classic.Logger logger : loggerContext.getLoggerList()) {
                java.util.Iterator<Appender<ILoggingEvent>> iter = logger.iteratorForAppenders();
                while (iter.hasNext()) {
                    Appender<ch.qos.logback.classic.spi.ILoggingEvent> appender = iter.next();
                    if (appender instanceof SMTPAppender) {
                        SMTPAppender smtpAppender = (SMTPAppender) appender;
                        logger.info("========== Logback SMTP Appender 配置 ==========");
                        logger.info("  Appender Name: {}", smtpAppender.getName());
                        logger.info("  SMTP Host: {}", smtpAppender.getSmtpHost());
                        logger.info("==============================================");
                        return; // 找到後就離開
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("無法取得 SMTP Appender 配置資訊", e);
        }
    }

    public AP95() throws Exception {
        // 設定環境變數與讀取設定檔
        setEnvProperties();
        // 設定資料庫連線
        setDataBaseConnection();
        // 印出 Logback SMTP Appender 配置
        printLogbackSmtpConfig();
    }

    public static void main( String[] args ) {
        try {
            // 設定xml
            //localXML = XmlLocalHandlerNewAP.performParser();
//            globalXML = XmlGlobalHandlerNewAP.performParser( 95, "" );
            // 設定file
//            fLogFile = new File( globalXML.getGlobalTagValue( "apini.logpath" ) + "AP95_" + new SimpleDateFormat( "yyyyMMdd" ).format( new Date() ) + ".log" );
//            fLogFile.createNewFile();
//            bwLogFile = new BufferedWriter( new FileWriter( fLogFile.getPath(), true ) );
//            pw = new PrintWriter( bwLogFile );
            logger.info("========== START 刊登超過60天時，自動將該職缺關閉後再開啟 ==========");
            AP95 ap95 = new AP95();
            ap95.queryData();
            logger.info("========== END 刊登超過60天時，自動將該職缺關閉後再開啟 ==========");
        } catch( Exception e ) {
//            logger.error("##不成功##", e);
            logger.error("main exception", e);
        } finally {
            //localXML = null;
//            globalXML = null;
            config = null;
        }
    }
}