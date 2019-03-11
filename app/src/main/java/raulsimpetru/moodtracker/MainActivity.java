package raulsimpetru.moodtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {
    private int[] monthLength;

    private ArrayList<Button>[] buttons;
    private TableRow[] trArray;
    private TableLayout layout;
    private int[][] mood = new int[12][31];
    private int day, month, year;
    private int buttonI, buttonJ;

    private File file;
    private FileOutputStream outputStream;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String[] date = currentDate.split("-");
        day = Integer.valueOf(date[0]);
        month = Integer.valueOf(date[1]);
        year = Integer.valueOf(date[2]);

        TextView tv = findViewById(R.id.YearText);
        tv.setText("Mood Tracker " + String.valueOf(year) + "\nmade by Raul C. Sîmpetru");

        //File set
        String fileName = String.valueOf(year) + ".csv";
        file = new File(this.getFilesDir(), fileName);

        if (!file.exists()) {

            Log.d("File", "Doesn't exist");
            try {
                outputStream = openFileOutput(fileName, MODE_PRIVATE);

                for (int i = 0; i < 12; i++) {
                    StringBuilder output = new StringBuilder();
                    for (int j = 0; j < 31; j++) {
                        mood[i][j] = -1;
                        output.append("-1");
                        if (j != 30)
                            output.append(";");
                        else
                            output.append("\n");
                    }
                    outputStream.write(output.toString().getBytes());
                }
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else try {
            String[] csvRow;
            CSVReader reader = new CSVReader(new FileReader(this.getFilesDir() + "/" + fileName), ';');

            List<String[]> csvAllRows = reader.readAll();
            for (int i = 0; i < csvAllRows.size(); i++) {
                csvRow = csvAllRows.get(i);
                for (int j = 0; j < 31; j++) {
                    mood[i][j] = Integer.valueOf(csvRow[j]);

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        monthLength = new int[12];

        for (int i = 0; i < monthLength.length; i++) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                monthLength[i] = YearMonth.of(year, i + 1).lengthOfMonth();
            else
                monthLength[i] = new GregorianCalendar(year, i, day).getActualMaximum(Calendar.DAY_OF_MONTH);

            Log.d("month", String.valueOf(monthLength[i]));
        }

        layout = findViewById(R.id.ButtonLayout);
        trArray = new TableRow[31];
        buttons = new ArrayList[monthLength.length];

        createUI();

    }

    private void createUI() {
        //Months
        TableRow monthTR = new TableRow(this);
        monthTR.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        //   JanFebMarAprMayJunJulAugSepOctNovDec
        String[] monthName = splitToNChar("   JanFebMarAprMayJunJulAugSepOctNovDec");
        TextView[] tvArray = new TextView[13];
        for (int i = 0; i < 13; i++) {
            tvArray[i] = new TextView(this);
            TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1f);
            params.setMargins(-25, 2, 10, 2);
            tvArray[i].setLayoutParams(params);
            Log.d("MonthName", monthName[i]);
            tvArray[i].setText(String.valueOf(monthName[i]));
            tvArray[i].setEllipsize(TruncateAt.END);
            tvArray[i].setTextSize(10f);
            tvArray[i].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            if (i != 0)
                tvArray[i].setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
            else
                tvArray[i].setTextColor(ContextCompat.getColor(this, R.color.colorBlack));
            monthTR.addView(tvArray[i]);
        }

        layout.addView(monthTR, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT, -0.1f));
        layout.setColumnStretchable(0, true);

        //Buttons
        for (int i = 0; i < monthLength.length; i++) {
            buttons[i] = new ArrayList<>();
            for (int j = 0; j < monthLength[i]; j++) {
                final Button button = new Button(this);
                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                params.setMargins(5, 5, 5, 5);
                button.setLayoutParams(params);

                if (i + 1 == month && j + 1 == day && mood[i][j] == -1)
                    button.setBackgroundResource(R.drawable.buttonshapetoday);
                else
                    changeButton(mood[i][j], button);

                final int finalI = i;
                final int finalJ = j;
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DateTime current = new DateTime();
                        if (new LocalDate(year, finalI + 1, finalJ + 1).toDate().before(current.toDate())) {
                            buttonI = finalI;
                            buttonJ = finalJ;
                            showNumberPicker(mood[buttonI][buttonJ]);
                        } else {
                            makeToast("How do you know how your day\nwill be tomorrow?" +
                                    "\nYou can't ... but nice try.", Toast.LENGTH_LONG);
                        }
                    }

                });

                buttons[i].add(button);
            }
            if (buttons[i].size() < 31) {
                int temp = 31 - buttons[i].size();
                for (int j = 0; j < temp; j++) {
                    Button _button = new Button(this);
                    TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                    params.setMargins(5, 5, 5, 5);
                    _button.setLayoutParams(params);
                    _button.setBackgroundResource(R.drawable.fakebuttonshape);
                    buttons[i].add(_button);
                }
            }
            Log.d("button", String.valueOf(buttons[i].size()));
        }

        //TableRows
        for (int i = 0; i < 31; i++) {
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.9f));
            trArray[i] = tr;
        }

        TextView[] dayTV = new TextView[31];

        for (int i = 0; i < 31; i++) {
            TableRow temp = trArray[i];
            Log.d("tableRow", String.valueOf(trArray.length));

            dayTV[i] = new TextView(this);
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, -0.05f);
            params.setMargins(0, 0, 15, 0);
            dayTV[i].setLayoutParams(params);
            dayTV[i].setText(String.valueOf(i + 1));
            dayTV[i].setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
            dayTV[i].setTextSize(15f);
            dayTV[i].setEllipsize(TruncateAt.END);
            dayTV[i].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            temp.addView(dayTV[i]);
            for (ArrayList<Button> button : buttons) {
                temp.addView(button.get(i));
            }
            layout.addView(temp, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            layout.setColumnStretchable(i + 1, true);
        }

        //Setting Button
        ImageButton button = findViewById(R.id.SettingsButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                }

                saveBitmap(screenShot(findViewById(R.id.ButtonLayout)), String.valueOf(year));
                makeToast("Export to jpeg successful!\nWell done!", Toast.LENGTH_SHORT);
            }
        });

    }

    public Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private static String[] splitToNChar(String text) {
        List<String> parts = new ArrayList<>();

        int length = text.length();
        for (int i = 0; i < length; i += 3) {
            parts.add(text.substring(i, Math.min(length, i + 3)));
        }
        return parts.toArray(new String[0]);
    }

    private void changeButton(int mood, Button _button) {
        switch (mood) {
            case -1:
                _button.setBackgroundResource(R.drawable.buttonshape);
                break;
            case 0:
                _button.setBackgroundResource(R.drawable.buttonshapedepressive);
                break;
            case 1:
                _button.setBackgroundResource(R.drawable.buttonshapemiserable);
                break;
            case 2:
                _button.setBackgroundResource(R.drawable.buttonshapeverybad);
                break;
            case 3:
                _button.setBackgroundResource(R.drawable.buttonshapebad);
                break;
            case 4:
                _button.setBackgroundResource(R.drawable.buttonshapenotthatwell);
                break;
            case 5:
                _button.setBackgroundResource(R.drawable.buttonshapealright);
                break;
            case 6:
                _button.setBackgroundResource(R.drawable.buttonshapeok);
                break;
            case 7:
                _button.setBackgroundResource(R.drawable.buttonshapegood);
                break;
            case 8:
                _button.setBackgroundResource(R.drawable.buttonshapeverygood);
                break;
            case 9:
                _button.setBackgroundResource(R.drawable.buttonshapegreat);
                break;
            case 10:
                _button.setBackgroundResource(R.drawable.buttonshapeawesome);
                break;
        }
    }

    public void showNumberPicker(int value) {
        NumberPickerDialog newFragment = new NumberPickerDialog();
        newFragment.setValue(value);
        newFragment.setValueChangeListener(this);
        newFragment.show(getSupportFragmentManager(), "number picker");
    }

    public void saveBitmap(Bitmap bitmap, String name) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/Camera/Mood Tracker";
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Mood Tracker-" + name + ".jpeg";
        File file = new File(myDir, fname);
        System.out.println(file.getAbsolutePath());
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaScannerConnection.scanFile(this, new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i1, int i2) {
        changeButton(numberPicker.getValue(), buttons[buttonI].get(buttonJ));
        mood[buttonI][buttonJ] = numberPicker.getValue();

        String fileName = String.valueOf(year) + ".csv";
        file = new File(this.getFilesDir(), fileName);
        if (file.exists()) {
            try {
                outputStream = openFileOutput(fileName, MODE_PRIVATE);

                for (int i = 0; i < 12; i++) {
                    StringBuilder output = new StringBuilder();
                    for (int j = 0; j < 31; j++) {
                        output.append(String.valueOf(mood[i][j]));
                        if (j != 30)
                            output.append(";");
                        else
                            output.append("\n");
                    }
                    outputStream.write(output.toString().getBytes());
                }
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        makeToast("Save Complete!", Toast.LENGTH_SHORT);
    }

    public void makeToast(String text, int lenght) {
        Toast toast = Toast.makeText(
                MainActivity.this,
                text,
                lenght);
        LinearLayout layout = (LinearLayout) toast.getView();
        if (layout.getChildCount() > 0) {
            TextView tv = (TextView) layout.getChildAt(0);
            tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        }
        toast.show();
    }

}
