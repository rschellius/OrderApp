package com.example.marni.orderapp.Presentation.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.jwt.JWT;
import com.example.marni.orderapp.BusinessLogic.DrawerMenu;
import com.example.marni.orderapp.BusinessLogic.TotalFromAssortment;
import com.example.marni.orderapp.DataAccess.Balance.BalanceGetTask;
import com.example.marni.orderapp.DataAccess.Orders.OrdersGetTask;
import com.example.marni.orderapp.DataAccess.Orders.OrdersPutTask;
import com.example.marni.orderapp.DataAccess.Product.ProductsDeleteTask;
import com.example.marni.orderapp.DataAccess.Product.ProductsGetTask;
import com.example.marni.orderapp.DataAccess.Product.ProductsPostTask;
import com.example.marni.orderapp.DataAccess.Product.ProductsPutTask;
import com.example.marni.orderapp.Domain.Balance;
import com.example.marni.orderapp.Domain.Order;
import com.example.marni.orderapp.Domain.Product;
import com.example.marni.orderapp.Presentation.Adapters.ProductsListviewAdapter;
import com.example.marni.orderapp.R;
import com.example.marni.orderapp.cardemulation.AccountStorage;

import java.text.DecimalFormat;
import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static com.example.marni.orderapp.Presentation.Activities.LogInActivity.JWT_STR;
import static com.example.marni.orderapp.Presentation.Activities.LogInActivity.USER;
import static com.example.marni.orderapp.Presentation.Activities.OrderHistoryActivity.ORDER;

public class MyOrderActivity extends AppCompatActivity implements
        TotalFromAssortment.OnTotalChanged,
        ProductsGetTask.OnProductAvailable, BalanceGetTask.OnBalanceAvailable, OrdersGetTask.OnOrderAvailable, ProductsListviewAdapter.OnMethodAvailable,
        ProductsPutTask.SuccessListener, ProductsPostTask.SuccessListener, ProductsDeleteTask.SuccessListener, OrdersPutTask.PutSuccessListener, NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = getClass().getSimpleName();

    private StickyListHeadersListView stickyList;

    private ArrayList<Product> products = new ArrayList<>();
    private ProductsListviewAdapter mAdapter;

    private double current_balance;
    private TextView textview_balance;

    private Order order;
    private double priceTotal;

    private JWT jwt;
    private int user;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        jwt = bundle.getParcelable(JWT_STR);
        user = bundle.getInt(USER);
        order = (Order) bundle.get(ORDER);

        getSupportActionBar().setTitle("My Order");
        toolbar.findViewById(R.id.toolbar_balance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TopUpActivity.class);
                intent.putExtra(JWT_STR, jwt);
                intent.putExtra(USER, user);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_my_order);

        ImageView imageView = (ImageView) findViewById(R.id.additem_orderdetail);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProductsActivity.class);
                intent.putExtra(JWT_STR, jwt);
                intent.putExtra(USER, user);
                startActivity(intent);
            }
        });

        stickyList = (StickyListHeadersListView) findViewById(R.id.listViewProducts);
        stickyList.setAreHeadersSticky(true);

        textview_balance = (TextView) findViewById(R.id.toolbar_balance);

        getBalance("https://mysql-test-p4.herokuapp.com/balance/" + user);
        getCurrentOrder("https://mysql-test-p4.herokuapp.com/order/current/" + user);
        getProducts("https://mysql-test-p4.herokuapp.com/products/order/" + order.getOrderId());

        if (savedInstanceState == null) {
            AccountStorage.SetAccount(this, "" + order.getOrderId());
        }
    }

    private void getCurrentOrder(String apiUrl) {

        OrdersGetTask task = new OrdersGetTask(this);
        String[] urls = new String[]{apiUrl, jwt.toString()};
        task.execute(urls);
    }

    @Override
    public void onOrderAvailable(Order order) {

        Boolean currentOrder = (order.getOrderId() == this.order.getOrderId());

        mAdapter = new ProductsListviewAdapter(getApplicationContext(), getLayoutInflater(), products, order, currentOrder, this, this);
        stickyList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    public void getBalance(String ApiUrl) {

        String[] urls = new String[]{ApiUrl, jwt.toString()};
        BalanceGetTask getBalance = new BalanceGetTask(this);
        getBalance.execute(urls);
    }

    public void onBalanceAvailable(Balance bal) {
        DecimalFormat formatter = new DecimalFormat("#0.00");

        current_balance = bal.getBalance();
        textview_balance.setText("€ " + formatter.format(current_balance));
    }

    public void getProducts(String ApiUrl) {

        ProductsGetTask task = new ProductsGetTask(this, "myorder");
        String[] urls = new String[]{ApiUrl, jwt.toString()};
        task.execute(urls);
    }

    @Override
    public void onProductAvailable(Product product) {

        products.add(product);
//        mAdapter.getAllergyIcons(product);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTotalChanged(Double priceTotal, int quantity) {
        this.priceTotal = priceTotal;

        DecimalFormat formatter = new DecimalFormat("#0.00");

        TextView textViewTotal = (TextView) findViewById(R.id.textViewTotal);
        TextView textViewQuantity = (TextView) findViewById(R.id.textViewTotalQuantity);
        textViewTotal.setText("€ " + formatter.format(priceTotal));
        textViewQuantity.setText(quantity + " items");
    }

    @Override
    public void onMethodAvailable(String method, Product product, Order order) {
        switch (method) {
            case "put":
                String[] urls = new String[]{"https://mysql-test-p4.herokuapp.com/product/quantity/edit", jwt.toString(), Integer.toString(order.getOrderId()), Integer.toString(product.getProductId()), user + "", Integer.toString(product.getQuantity())};
                ProductsPutTask putProduct = new ProductsPutTask(this);
                putProduct.execute(urls);

                break;
            case "post":
                String[] urls2 = new String[]{"https://mysql-test-p4.herokuapp.com/product/quantity/add", jwt.toString(), Integer.toString(order.getOrderId()), Integer.toString(product.getProductId()), user + "", Integer.toString(product.getQuantity())};
                ProductsPostTask postProduct = new ProductsPostTask(this);
                postProduct.execute(urls2);
                break;
            case "delete":
                String[] urls3 = new String[]{"https://mysql-test-p4.herokuapp.com/product/quantity/delete", jwt.toString(), Integer.toString(order.getOrderId()), Integer.toString(product.getProductId()), user + ""};
                ProductsDeleteTask deleteProduct = new ProductsDeleteTask(this);
                deleteProduct.execute(urls3);
        }

        String[] urls = new String[]{"https://mysql-test-p4.herokuapp.com/order/price/edit", jwt.toString(), priceTotal + "", Integer.toString(order.getOrderId())};
        OrdersPutTask putOrder = new OrdersPutTask(this);
        putOrder.execute(urls);
    }

    @Override
    public void successful(Boolean successful) {
        if (successful) {
            Toast.makeText(this, "Product amount changed", Toast.LENGTH_SHORT).show();
            products.clear();
            getProducts("https://mysql-test-p4.herokuapp.com/products/order/" + order.getOrderId());
        } else {
            Toast.makeText(this, "Product quantity couldn't be changed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void putSuccessful(Boolean successful) {
        if (successful) {
            Toast.makeText(this, "Product amount changed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Product quantity couldn't be changed", Toast.LENGTH_SHORT).show();
        }
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

        new DrawerMenu(getApplicationContext(), id, jwt, user);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}