package com.mobi.donation.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mobi.donation.api.DonationApi;
import com.mobi.donation.models.Donation;
import com.mobi.myfirstapplication.R;

import java.time.LocalDate;
import java.util.List;

public class Report extends Base {
    ListView listView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    DonationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        listView = (ListView) findViewById(R.id.report_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout)
                findViewById(R.id.report_swipe_refresh_layout);
        new GetAllTask(this).execute("/donations");
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new GetAllTask(Report.this).execute("/donations");
                    }
                }
        );

    }


    //    DONATION ADAPTER
    class DonationAdapter extends ArrayAdapter<Donation> {
        private final Context context;

        public List<Donation> donations;

        public DonationAdapter(Context context, List<Donation> donations) {
            super(context, R.layout.row_donate, donations);
            this.context = context;
            this.donations = donations;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("ViewHolder")
            View view = inflater.inflate(R.layout.row_donate, parent, false);
            Donation donation = donations.get(position);

            TextView amountView = (TextView) view.findViewById(R.id.row_amount);
            TextView methodView = (TextView) view.findViewById(R.id.row_method);
            TextView upvotesView = (TextView) view.findViewById(R.id.row_upvotes);
            TextView dateView = (TextView) view.findViewById(R.id.date);

            amountView.setText(String.format("$%d", donation.amount));
            methodView.setText(donation.paymentType);
            upvotesView.setText(String.valueOf(donation.upvotes));
            dateView.setText(String.valueOf(LocalDate.now()));
            view.setTag(donation.id);

            ImageView imgDelete = (ImageView) view.findViewById(R.id.imgDelete);
            imgDelete.setTag(donation);
            imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v("tag", String.valueOf(view.getTag()));
                    if (view.getTag() instanceof Donation) {
                        onDonationDelete((Donation) view.getTag());
                    }
                }
            });

            return view;
        }

        @Override
        public int getCount() {
            return donations.size();
        }
    }

    public void onDonationDelete(final Donation donation) {
        String stringId = donation.id;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Donation?");
        builder.setIcon(android.R.drawable.ic_delete);
        builder.setMessage("Are you sure you want to Delete the 'Donation with ID' \n [ "
                + stringId + " ] ?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                new DeleteTask(Report.this).execute("/donations", donation.id);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    //    GET ALL
    private class GetAllTask extends AsyncTask<String, Void, List<Donation>> {
        protected ProgressDialog dialog;
        protected Context context;

        public GetAllTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Retrieving Donations List");
            this.dialog.show();
        }

        @Override
        protected List<Donation> doInBackground(String... params) {
            try {
                return (List<Donation>) DonationApi.getAll((String) params[0]);
            } catch (Exception e) {
                Log.v("ASYNC", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Donation> result) {
            super.onPostExecute(result);
            app.donations = result;
            adapter = new DonationAdapter(context, app.donations);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View donate, int i, long l) {
                    String _id = donate.getTag().toString();
                    new GetTask(Report.this).execute("/donations", _id);
                }
            });
            mSwipeRefreshLayout.setRefreshing(false);

            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    //    GET
    private class GetTask extends AsyncTask<String, Void, Donation> {
        protected ProgressDialog dialog;
        protected Context context;

        public GetTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Retrieving Donation Details");
            this.dialog.show();
        }

        @Override
        protected Donation doInBackground(String... params) {
            try {
                return (Donation) DonationApi.get((String) params[0], (String)
                        params[1]);
            } catch (Exception e) {
                Log.v("donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Donation result) {
            super.onPostExecute(result);
            Donation donation = result;
            Toast.makeText(Report.this, "Donation Data [ " + donation.upvotes +
                    "]\n " +
                    "With ID of [" + donation.id + "]", Toast.LENGTH_LONG).show();
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    //  DELETE
    private class DeleteTask extends AsyncTask<String, Void, String> {
        protected ProgressDialog dialog;
        protected Context context;

        public DeleteTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Deleting Donation");
            this.dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return (String) DonationApi.delete((String) params[0], (String)
                        params[1]);
            } catch (Exception e) {
                Log.v("donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String s = result;
            Log.v("donate", "DELETE REQUEST : " + s);
            new GetAllTask(Report.this).execute("/donations");
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }


}
