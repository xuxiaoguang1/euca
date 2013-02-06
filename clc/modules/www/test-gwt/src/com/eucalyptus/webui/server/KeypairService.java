package com.eucalyptus.webui.server;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.apache.log4j.Logger;

import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.shared.dictionary.DBTableColName;
import com.eucalyptus.webui.shared.dictionary.DBTableName;

public class KeypairService {
  private static KeypairService instance = null;
  private static final Logger LOG = Logger.getLogger(AwsServiceImpl.class);
  private static final DBProcWrapper wrapper = DBProcWrapper.Instance();
  private KeypairService() {
    
  }
  public static KeypairService getInstance() {
    if (instance == null)
      instance = new KeypairService();
    return instance;
  }
  
  void insert(int userID, String name, String value) {
    value = value.replace("\n", "\\n");
    String text = name + userID;
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      digest.update(text.getBytes("UTF-8"));
      Random rnd = new Random();
      byte[] padding = new byte[10];
      rnd.nextBytes(padding);
      digest.update(padding);
      byte[] d = digest.digest();
      BigInteger bigInt = new BigInteger(1, d);
      String hash = bigInt.toString(16);
      
      //insert to db
      StringBuilder sb = new StringBuilder();
      
      final String VALUE = DBTableColName.USER_KEYPAIR.VALUE;
      final String HASH = DBTableColName.USER_KEYPAIR.HASH;
      sb.append("INSERT INTO ").append(DBTableName.USER_KEYPAIR).append("(")
        .append(DBTableColName.USER_KEYPAIR.NAME).append(",")
        .append(DBTableColName.USER_KEYPAIR.VALUE).append(",")
        .append(DBTableColName.USER_KEYPAIR.HASH).append(",")
        .append(DBTableColName.USER_KEYPAIR.USER_ID).append(") VALUES (")
        .append("'").append(name).append("',")
        .append("'").append(value).append("',")
        .append("'").append(hash).append("',")
        .append("'").append(userID).append("')")
        .append(" ON DUPLICATE KEY UPDATE ")
        .append(VALUE).append("=VALUES(").append(VALUE).append("),")
        .append(HASH).append("=VALUES(").append(HASH).append(")");

      wrapper.update(sb.toString()); 
    } catch (NoSuchAlgorithmException e) {
      
    } catch (UnsupportedEncodingException e) {
      
    } catch (SQLException e) {
      
    }

  }
  
  String[] select(String hash) {
    StringBuilder sb = new StringBuilder();
    String NAME = DBTableColName.USER_KEYPAIR.NAME;
    String VALUE = DBTableColName.USER_KEYPAIR.VALUE;
    String HASH = DBTableColName.USER_KEYPAIR.HASH;
    sb.append("SELECT ").append(NAME).append(", ").append(VALUE)
      .append(" FROM ").append(DBTableName.USER_KEYPAIR).append(" WHERE ")
      .append(HASH).append(" = ").append("'").append(hash).append("'");
    try {
      ResultSet ret = wrapper.query(sb.toString()).getResultSet();
      if (ret.first()) {
        String[] r = {ret.getString(NAME), ret.getString(VALUE)};
        return r;
      }
    } catch (SQLException e) {
    }
    return null;

  }
  
  String select(int userID, String name) {
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT ").append(DBTableColName.USER_KEYPAIR.HASH)
      .append(" FROM ").append(DBTableName.USER_KEYPAIR)
      .append(" WHERE ").append(DBTableColName.USER_KEYPAIR.USER_ID).append(" = '").append(userID).append("' AND ")
      .append(DBTableColName.USER_KEYPAIR.NAME).append(" = '").append(name).append("'");
    try {
      ResultSet ret = wrapper.query(sb.toString()).getResultSet();
      if (ret.first()) {
        return ret.getString(DBTableColName.USER_KEYPAIR.HASH);
      }
    } catch (SQLException e) {
      
    }
    return null;
  }

}
