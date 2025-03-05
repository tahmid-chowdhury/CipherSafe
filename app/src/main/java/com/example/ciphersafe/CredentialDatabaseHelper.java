package com.example.ciphersafe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CredentialDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "CredentialDBHelper";
    private Context context; // Store context for encryption/decryption

    // Database Info
    private static final String DATABASE_NAME = "PasswordManager.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_CREDENTIALS = "credentials";

    // Credential Table Columns
    private static final String KEY_CREDENTIAL_ID = "id";
    private static final String KEY_CREDENTIAL_SERVICE_NAME = "service_name";
    private static final String KEY_CREDENTIAL_USERNAME = "username";
    private static final String KEY_CREDENTIAL_PASSWORD = "password";
    private static final String KEY_CREDENTIAL_LAST_MODIFIED = "last_modified";
    private static final String KEY_CREDENTIAL_USER_ID = "user_id"; // New column for user association

    // Singleton instance
    private static CredentialDatabaseHelper instance;

    // Singleton pattern to get a single instance of the database helper
    public static synchronized CredentialDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CredentialDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private CredentialDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context; // Store context
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CREDENTIALS_TABLE = "CREATE TABLE " + TABLE_CREDENTIALS +
                "(" +
                KEY_CREDENTIAL_ID + " TEXT PRIMARY KEY," +
                KEY_CREDENTIAL_SERVICE_NAME + " TEXT," +
                KEY_CREDENTIAL_USERNAME + " TEXT," +
                KEY_CREDENTIAL_PASSWORD + " TEXT," +
                KEY_CREDENTIAL_LAST_MODIFIED + " INTEGER," +
                KEY_CREDENTIAL_USER_ID + " TEXT" + // Add user ID column
                ")";

        db.execSQL(CREATE_CREDENTIALS_TABLE);
        Log.d(TAG, "Created database tables");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // For an upgrade from version without user_id column to one with it
            if (oldVersion == 1 && newVersion == 2) {
                db.execSQL("ALTER TABLE " + TABLE_CREDENTIALS +
                        " ADD COLUMN " + KEY_CREDENTIAL_USER_ID + " TEXT");
            } else {
                // Complete database rebuild
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CREDENTIALS);
                onCreate(db);
            }
        }
    }

    // CRUD operations for Credentials

    // Add new credential
    public void addCredential(Credential credential) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_CREDENTIAL_ID, credential.getId());
            values.put(KEY_CREDENTIAL_SERVICE_NAME, credential.getServiceName());
            values.put(KEY_CREDENTIAL_USERNAME, credential.getUsername());
            // Encrypt password before storing
            String encryptedPassword = EncryptionUtil.encrypt(context, credential.getPassword());
            values.put(KEY_CREDENTIAL_PASSWORD, encryptedPassword);
            values.put(KEY_CREDENTIAL_LAST_MODIFIED, credential.getLastModified());
            values.put(KEY_CREDENTIAL_USER_ID, credential.getUserId()); // Store user ID

            db.insertOrThrow(TABLE_CREDENTIALS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error while adding credential", e);
        } finally {
            db.endTransaction();
        }
    }

    // Get all credentials for a specific user
    public List<Credential> getCredentialsForUser(String userId) {
        List<Credential> credentials = new ArrayList<>();

        String CREDENTIALS_SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = ? ORDER BY %s DESC",
                TABLE_CREDENTIALS, KEY_CREDENTIAL_USER_ID, KEY_CREDENTIAL_LAST_MODIFIED);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(CREDENTIALS_SELECT_QUERY, new String[]{userId});

        try {
            if (cursor.moveToFirst()) {
                do {
                    Credential credential = new Credential();
                    credential.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREDENTIAL_ID)));
                    credential.setServiceName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREDENTIAL_SERVICE_NAME)));
                    credential.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREDENTIAL_USERNAME)));

                    // Decrypt password when reading from database
                    String encryptedPassword = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREDENTIAL_PASSWORD));
                    String decryptedPassword = EncryptionUtil.decrypt(context, encryptedPassword);
                    credential.setPassword(decryptedPassword);

                    credential.setLastModified(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CREDENTIAL_LAST_MODIFIED)));
                    credential.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREDENTIAL_USER_ID)));

                    credentials.add(credential);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting credentials for user", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return credentials;
    }

    // Get all credentials (for backward compatibility or admin use)
    public List<Credential> getAllCredentials() {
        List<Credential> credentials = new ArrayList<>();

        String CREDENTIALS_SELECT_QUERY = String.format("SELECT * FROM %s ORDER BY %s DESC",
                TABLE_CREDENTIALS, KEY_CREDENTIAL_LAST_MODIFIED);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(CREDENTIALS_SELECT_QUERY, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    Credential credential = new Credential();
                    credential.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREDENTIAL_ID)));
                    credential.setServiceName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREDENTIAL_SERVICE_NAME)));
                    credential.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREDENTIAL_USERNAME)));

                    // Decrypt password when reading from database
                    String encryptedPassword = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREDENTIAL_PASSWORD));
                    String decryptedPassword = EncryptionUtil.decrypt(context, encryptedPassword);
                    credential.setPassword(decryptedPassword);

                    credential.setLastModified(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CREDENTIAL_LAST_MODIFIED)));

                    // Get user_id if it exists in the table (handle potential missing column)
                    try {
                        credential.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREDENTIAL_USER_ID)));
                    } catch (IllegalArgumentException e) {
                        credential.setUserId(null); // Set null for older database entries
                    }

                    credentials.add(credential);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting all credentials", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return credentials;
    }

    // Update credential
    public int updateCredential(Credential credential) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CREDENTIAL_SERVICE_NAME, credential.getServiceName());
        values.put(KEY_CREDENTIAL_USERNAME, credential.getUsername());
        // Encrypt password before updating
        String encryptedPassword = EncryptionUtil.encrypt(context, credential.getPassword());
        values.put(KEY_CREDENTIAL_PASSWORD, encryptedPassword);
        values.put(KEY_CREDENTIAL_LAST_MODIFIED, System.currentTimeMillis());

        // Update user ID if present
        if (credential.getUserId() != null) {
            values.put(KEY_CREDENTIAL_USER_ID, credential.getUserId());
        }

        return db.update(TABLE_CREDENTIALS, values, KEY_CREDENTIAL_ID + " = ?",
                new String[]{credential.getId()});
    }

    // Delete credential
    public void deleteCredential(String credentialId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CREDENTIALS, KEY_CREDENTIAL_ID + " = ?", new String[]{credentialId});
    }

    // Delete all credentials for a specific user
    public void deleteCredentialsForUser(String userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CREDENTIALS, KEY_CREDENTIAL_USER_ID + " = ?", new String[]{userId});
    }
}