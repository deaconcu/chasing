using UnityEngine;
using System;
using System.Data;
using Mono.Data.Sqlite;
using System.IO;

public class DbService {

	/*
	 * 如果数据库不存在，创建数据库和数据表
	 */
	public static void createDbIfNotExist() {
		Directory.CreateDirectory(Application.persistentDataPath + "/db");
		string dbFile = Application.persistentDataPath + "/db/chasing.db";
		if (File.Exists (dbFile)) {
			// TODO 
			//File.Delete (dbFile);
			return;
		}
		SqliteConnection.CreateFile(dbFile);
		string conn = "URI=file:" + dbFile; //Path to database.
		Debug.Log(conn);

		IDbConnection dbconn = null;
		try {
			dbconn = (IDbConnection)new SqliteConnection(conn);
			dbconn.Open(); //Open connection to the database.
			IDbCommand dbcmd = dbconn.CreateCommand();

			string sqlQuery = "create table user (id INTEGER, password TEXT)";
			Debug.Log("execute:" + sqlQuery);
			dbcmd.CommandText = sqlQuery;
			dbcmd.ExecuteNonQuery();
			dbcmd.Dispose();
		} catch (Exception e) {
			Debug.Log (e);
			throw e;
		} finally {
			if (dbconn != null) {
				dbconn.Close();
			}
		}
	}

	public static void insertUser(int id, string password)
    {
		string conn = "URI=file:" + Application.persistentDataPath + "/db/chasing.db"; //Path to database.
        Debug.Log(conn);

		IDbConnection dbconn = null;
		try {
	        dbconn = (IDbConnection)new SqliteConnection(conn);
	        dbconn.Open();
	        IDbCommand dbcmd = dbconn.CreateCommand();
	        string sqlQuery = "insert into user values(@id, @password)";
	        dbcmd.CommandText = sqlQuery;

	        IDbDataParameter idParam = dbcmd.CreateParameter();
	        idParam.ParameterName = "@id";
	        idParam.Value = id;
			IDbDataParameter passwordParam = dbcmd.CreateParameter();
			passwordParam.ParameterName = "@password";
			passwordParam.Value = password;
	        dbcmd.Parameters.Add(idParam);
			dbcmd.Parameters.Add(passwordParam);
	        dbcmd.ExecuteNonQuery();

	        dbcmd.Dispose();
	        dbcmd = null;
		} catch (Exception e) {
			Debug.Log (e);
			throw e;
		} finally {
			if (dbconn != null) {
				dbconn.Close();
				dbconn = null;
			}
		}
    }

	public static User getUser()
    {
		string conn = "URI=file:" + Application.persistentDataPath + "/db/chasing.db"; //Path to database.
        Debug.Log(conn);

		IDbConnection dbconn = null;
		try {
	        dbconn = (IDbConnection)new SqliteConnection(conn);
	        dbconn.Open(); //Open connection to the database.
	        IDbCommand dbcmd = dbconn.CreateCommand();

	        string sqlQuery = "SELECT id, password FROM user";
	        dbcmd.CommandText = sqlQuery;
	        IDataReader reader = dbcmd.ExecuteReader();
	        int dbId = 0;
			string password = "";
	        if (reader.Read())
	        {
	            dbId = reader.GetInt32(0);
				password = reader.GetString(1);
				return new User(dbId, password);
	        }
	        reader.Close();
	        dbcmd.Dispose();
		} catch (Exception e) {
			Debug.Log (e);
			throw e;
		} finally {
			if (dbconn != null) {
				dbconn.Close();
				dbconn = null;
			}
		}
		return null;
    }
}
