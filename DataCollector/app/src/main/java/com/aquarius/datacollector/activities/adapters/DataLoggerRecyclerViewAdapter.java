package com.aquarius.datacollector.activities.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aquarius.datacollector.R;
import com.aquarius.datacollector.activities.fragments.DataFilesFragment.OnListFragmentInteractionListener;
import com.aquarius.datacollector.database.DataLogger;
import com.aquarius.datacollector.dummy.DummyContent.DummyItem;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class DataLoggerRecyclerViewAdapter extends RealmRecyclerViewAdapter<DataLogger, DataLoggerRecyclerViewAdapter.MyViewHolder> {

       public DataLoggerRecyclerViewAdapter(OrderedRealmCollection<DataLogger> data) {
            super(data, true);
            setHasStableIds(true);
        }

        // Deletion mode code is in MyDataFileRecyclerView if needed for this adapter

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.dataloggers_list_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final DataLogger obj = getItem(position);
            //noinspection ConstantConditions
            holder.uuid.setText("UUID:" + obj.getUUID());
            Date lastDownloadDate = new Date(Long.parseLong(obj.getLastDownloadedFileDate()) * 1000);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm zzz");
            String lastDownloadDateString = dateFormat.format(lastDownloadDate);
            holder.lastDownloadDate.setText("Last Download: "  + lastDownloadDateString);
            holder.description.setText(obj.getDescription());
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView uuid;
            TextView lastDownloadDate;
            TextView description;

            MyViewHolder(View view) {
                super(view);
                uuid = (TextView) view.findViewById(R.id.uuid);
                lastDownloadDate = (TextView) view.findViewById(R.id.last_download_date);
                description = (TextView) view.findViewById(R.id.description);
            }
        }
    }