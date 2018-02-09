package zyuezheng.mdpgp10;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ReconfigurationActivity extends Activity {

    private Button saveBtn;
    private Button cancelBtn;
    private EditText f1EditText;
    private EditText f2EditText;
    private String f1Content;
    private String f2Content;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_reconfiguration);

        saveBtn = (Button)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                save();
                finish();
            }
        });

        cancelBtn = (Button)findViewById(R.id.cancelReconfiguration);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        f1EditText = (EditText)findViewById(R.id.f1EditText);
        f1EditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocused) {
                if(!hasFocused)
                    hideKeyboard(v);
            }
        });

        f2EditText = (EditText)findViewById(R.id.f2EditText);
        f2EditText.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocused){
                if(!hasFocused)
                    hideKeyboard(v);
            }
        });

        loadPreferences();

    }

    private void loadPreferences(){
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        f1Content = preferences.getString("F1String","");
        f2Content = preferences.getString("F2String","");

        f1EditText.setText(f1Content);
        f2EditText.setText(f2Content);
    }

    private void save(){
        super.onPause();
        android.content.SharedPreferences.Editor editor = preferences.edit();
        editor.putString("F1String", f1EditText.getText().toString());
        editor.putString("F2String", f2EditText.getText().toString());

        editor.commit();
        Toast.makeText(getApplicationContext(), "Reconfigured", Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard(View v){
        if(v != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromInputMethod(v.getWindowToken(), 0);
        }
    }

}
