package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_FILE = "student_profile";

    // הצהרה על משתני המחלקה כדי ש-initViews יכיר אותם
    private EditText etFirstName, etLastName, etEmail;
    private RadioGroup rgGrade;
    private TextView tvGradeTitle, tvMajorTitle, tvResult;
    private Spinner spMajor;
    private CheckBox cbNotifications, cbTerms;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initMajorSpinner();
        initListeners();

        // טעינת נתונים שמורים אם קיימים בהפעלה הראשונה
        Student savedStudent = loadStudent();
        if (savedStudent != null) {
            showStudent(savedStudent);
        }
    }

    private void initViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        rgGrade = findViewById(R.id.rgGrade);
        tvGradeTitle = findViewById(R.id.tvGradeTitle);
        tvMajorTitle = findViewById(R.id.tvMajorTitle);
        spMajor = findViewById(R.id.spMajor);
        cbNotifications = findViewById(R.id.cbNotifications);
        cbTerms = findViewById(R.id.cbTerms);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvResult = findViewById(R.id.tvResult);
    }

    private void initMajorSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.major_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMajor.setAdapter(adapter);
    }

    private void initListeners() {
        btnSubmit.setOnClickListener(v -> submitForm());
    }

    private void submitForm() {
        tvResult.setVisibility(View.GONE);

        if (!validateTextFields()) {
            return;
        }

        String grade = getSelectedGrade();
        if (grade == null) {
            tvGradeTitle.setError(getString(R.string.error_select_grade));
            Toast.makeText(this, R.string.error_select_grade, Toast.LENGTH_SHORT).show();
            return;
        }
        tvGradeTitle.setError(null);

        String major = getSelectedMajor();
        if (major == null) {
            tvMajorTitle.setError(getString(R.string.error_select_major));
            Toast.makeText(this, R.string.error_select_major, Toast.LENGTH_SHORT).show();
            return;
        }
        tvMajorTitle.setError(null);

        if (!cbTerms.isChecked()) {
            cbTerms.setError(getString(R.string.error_accept_terms));
            cbTerms.requestFocus();
            return;
        }
        cbTerms.setError(null);

        boolean notifications = cbNotifications.isChecked();

        String first = etFirstName.getText().toString().trim();
        String last = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        Student student = new Student(first, last, email, grade, major, notifications);

        showStudent(student);
        saveStudent(student);

        tvResult.setVisibility(View.VISIBLE);
    }

    private boolean validateTextFields() {
        String first = etFirstName.getText().toString().trim();
        String last = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (first.isEmpty()) {
            etFirstName.setError(getString(R.string.error_first_name));
            etFirstName.requestFocus();
            return false;
        }
        etFirstName.setError(null);

        if (last.isEmpty()) {
            etLastName.setError(getString(R.string.error_last_name));
            etLastName.requestFocus();
            return false;
        }
        etLastName.setError(null);

        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.error_email_required));
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_email_invalid));
            etEmail.requestFocus();
            return false;
        }
        etEmail.setError(null);

        return true;
    }

    private String getSelectedGrade() {
        int selectedId = rgGrade.getCheckedRadioButtonId();
        if (selectedId == -1) {
            return null;
        }
        RadioButton selectedButton = findViewById(selectedId);
        return selectedButton.getText().toString();
    }

    private String getSelectedMajor() {
        int position = spMajor.getSelectedItemPosition();
        if (position <= 0) {
            return null;
        }
        return spMajor.getSelectedItem().toString();
    }

    private void showStudent(Student student) {
        String summary = getString(
                R.string.student_summary,
                student.getFullName(),
                student.getEmail(),
                student.getGrade(),
                student.getMajor()
        );
        tvResult.setText(summary);
        tvResult.setVisibility(View.VISIBLE);
    }

    private void saveStudent(Student s) {
        SharedPreferences prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        prefs.edit()
                .putString("first_name", s.getFirstName())
                .putString("last_name", s.getLastName())
                .putString("email", s.getEmail())
                .putString("grade", s.getGrade())
                .putString("major", s.getMajor())
                .putBoolean("notifications_enabled", s.isNotificationsEnabled())
                .apply();
    }

    private Student loadStudent() {
        SharedPreferences prefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        String first = prefs.getString("first_name", null);
        String last = prefs.getString("last_name", null);
        String email = prefs.getString("email", null);
        String grade = prefs.getString("grade", null);
        String major = prefs.getString("major", null);

        if (isMissing(first) || isMissing(last) || isMissing(email) || isMissing(grade) || isMissing(major)) {
            return null;
        }

        boolean notifications = prefs.getBoolean("notifications_enabled", false);
        return new Student(first, last, email, grade, major, notifications);
    }

    private boolean isMissing(String value) {
        return value == null || value.trim().isEmpty();
    }
}