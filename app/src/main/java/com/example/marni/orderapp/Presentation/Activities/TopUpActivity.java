package com.example.marni.orderapp.Presentation.Activities;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marni.orderapp.BusinessLogic.CalculateBalance;
import com.example.marni.orderapp.DataAccess.Balance.BalanceGetTask;
import com.example.marni.orderapp.DataAccess.Balance.BalancePostTask;
import com.example.marni.orderapp.DataAccess.Orders.OrdersGetCurrentTask;
import com.example.marni.orderapp.Domain.Balance;
import com.example.marni.orderapp.BusinessLogic.DrawerMenu;
import com.example.marni.orderapp.Domain.Order;
import com.example.marni.orderapp.R;
import com.example.marni.orderapp.cardemulation.AccountStorage;

import java.text.DecimalFormat;

import static android.R.color.darker_gray;
import static android.R.color.holo_green_light;

public class TopUpActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        CalculateBalance.OnBalanceChanged, CalculateBalance.OnResetBalance, BalanceGetTask.OnBalanceAvailable,
        BalancePostTask.SuccessListener, CalculateBalance.OnCheckPayment, OrdersGetCurrentTask.OnCurrentOrderAvailable {

    private final String TAG = getClass().getSimpleName();
    private RadioButton button1, button2;
    private TextView textview_balance, textview_newbalance;
    private EditText edittext_value;
    private double current_balance;
    private CalculateBalance calculateBalance;
    private Spinner spinner;
    private Button payment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        // hide title
        getSupportActionBar().setTitle("Top Up");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // set current menu item checked
        navigationView.setCheckedItem(R.id.nav_top_up);

        getBalance();
        getCurrentOrder("https://mysql-test-p4.herokuapp.com/order/current/284");

        calculateBalance = new CalculateBalance(this, this, this);

        button1 = (RadioButton)findViewById(R.id.topup_radiobutton1);
        button1.setChecked(true);
        button2 = (RadioButton)findViewById(R.id.topup_radiobutton2);

        payment = (Button)findViewById(R.id.topup_button_topayment);
        payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postBalance("https://mysql-test-p4.herokuapp.com/topup");
            }
        });

        textview_balance = (TextView)findViewById(R.id.toolbar_balance);
        textview_newbalance = (TextView)findViewById(R.id.topup_edittext_newbalance);

        edittext_value = (EditText)findViewById(R.id.topup_edittext_value);
        edittext_value.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(3)
        });

        spinner = (Spinner)findViewById(R.id.topup_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(button2.isChecked()) {
                    switch (position) {
                        case 0:
                            addBalance(Integer.parseInt(spinner.getSelectedItem().toString()));
                            break;
                        case 1:
                            addBalance(Integer.parseInt(spinner.getSelectedItem().toString()));
                            break;
                        case 2:
                            addBalance(Integer.parseInt(spinner.getSelectedItem().toString()));
                            break;
                        case 3:
                            addBalance(Integer.parseInt(spinner.getSelectedItem().toString()));
                            break;
                        case 4:
                            addBalance(Integer.parseInt(spinner.getSelectedItem().toString()));
                            break;
                    }

                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        edittext_value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if(button1.isChecked()){
                        Integer add_balance = Integer.parseInt(edittext_value.getText().toString());
                        addBalance(add_balance);
                    }
                } catch (Exception e){
                    Log.i(TAG, "Empty value");
                    Toast.makeText(getApplicationContext(), "Select valid amount", Toast.LENGTH_SHORT).show();
                    calculateBalance.resetBalance(true);
                }
            }
        });
    }

        @Override
        public void onBackPressed() {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.toolbar_menu, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                default:
                    // If we got here, the user's action was not recognized.
                    // Invoke the superclass to handle it.
                    return super.onOptionsItemSelected(item);
            }
        }

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Log.i(TAG, item.toString() + " clicked.");

            int id = item.getItemId();

            new DrawerMenu(getApplicationContext(), id);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.topup_radiobutton1:
                if (checked)
                    button2.setChecked(false);
                    calculateBalance.resetBalance(true);
                    edittext_value.setEnabled(true);

                    break;
            case R.id.topup_radiobutton2:
                if (checked)
                    button1.setChecked(false);
                    calculateBalance.resetBalance(false);
                    edittext_value.setText("");
                    edittext_value.setEnabled(false);
                    spinner.setFocusable(true);

                    String balance = spinner.getSelectedItem().toString();
                    Integer add_balance = Integer.parseInt(balance);
                    addBalance(add_balance);

                    break;
        }
    }

    public void getBalance(){
        String[] urls = new String[] { "https://mysql-test-p4.herokuapp.com/balance/284" };

        BalanceGetTask getBalance = new BalanceGetTask(this);
        getBalance.execute(urls);
    }

    private void getCurrentOrder(String apiUrl) {

        OrdersGetCurrentTask task = new OrdersGetCurrentTask(this);
        String[] urls = new String[]{apiUrl};
        task.execute(urls);
    }

    @Override
    public void onCurrentOrderAvailable(Order order) {
        AccountStorage.SetAccount(this, "" + order.getOrderId());
    }

    @Override
    public void onBalanceChanged(double newBalance) {
        DecimalFormat formatter = new DecimalFormat("#0.00");

        calculateBalance.checkPayment();

        if(calculateBalance.getAddedBalance() != 0){
            textview_newbalance.setText("€ " + formatter.format(newBalance));
        }
    }

    @Override
    public void onResetBalance(double balance) {
        textview_newbalance.setText("");
        payment.setBackgroundColor(getResources().getColor(darker_gray));
        payment.setEnabled(false);
    }

    public void addBalance(int balance){
        calculateBalance.newBalance(current_balance, balance);
    }

    public void onBalanceAvailable(Balance bal){
        DecimalFormat formatter = new DecimalFormat("#0.00");

        current_balance = bal.getBalance();
        textview_balance.setText("€ " + formatter.format(current_balance));
    }

    public void postBalance(String ApiUrl){
        BalancePostTask task = new BalancePostTask(this);
        String[] urls = new String[]{ApiUrl, Double.toString(calculateBalance.getAddedBalance()), "topup", "284"};
        task.execute(urls);
    }

    public void SuccesfulTopUp(){
        calculateBalance.resetBalance(true);
        edittext_value.setText("");
        edittext_value.setEnabled(true);
        button1.setChecked(true);
        button2.setChecked(false);
        spinner.setSelection(0);
    }

    @Override
    public void successful(Boolean successful) {
        Log.i(TAG, successful.toString());
        if(successful){
            Toast.makeText(this, "Balance succesfully added", Toast.LENGTH_SHORT).show();
            SuccesfulTopUp();
            getBalance();
        } else {
            Toast.makeText(this, "Balance top up failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCheckPayment(String check) {
        if(check.equals("succes")) {
            payment.setBackgroundColor(getResources().getColor(holo_green_light));
            payment.setEnabled(true);
        } else if (check.equals("zero")){
            Toast.makeText(this, "Select valid amount", Toast.LENGTH_SHORT).show();
            payment.setBackgroundColor(getResources().getColor(darker_gray));
            payment.setEnabled(false);
        } else {
            Toast.makeText(this, "Max account balance is 150", Toast.LENGTH_SHORT).show();
            payment.setBackgroundColor(getResources().getColor(darker_gray));
            payment.setEnabled(false);
        }
    }
}
