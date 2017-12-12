package com.aquarius.datacollector.activities.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aquarius.datacollector.R;
import com.aquarius.datacollector.activities.fragments.DataFilesFragment.OnListFragmentInteractionListener;
import com.aquarius.datacollector.database.DataLog;
import com.aquarius.datacollector.dummy.DummyContent.DummyItem;

import java.util.HashSet;
import java.util.Set;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyDataFileRecyclerViewAdapter  extends RealmRecyclerViewAdapter<DataLog, MyDataFileRecyclerViewAdapter.MyViewHolder> {

        private boolean inDeletionMode = false;  // Not Used
        private Set<Integer> countersToDelete = new HashSet<Integer>(); // Not used

       public MyDataFileRecyclerViewAdapter(OrderedRealmCollection<DataLog> data) {
            super(data, true);
            setHasStableIds(true);
        }

        void enableDeletionMode(boolean enabled) {
            inDeletionMode = enabled;
            if (!enabled) {
                countersToDelete.clear();
            }
            notifyDataSetChanged();
        }

        Set<Integer> getCountersToDelete() {
            return countersToDelete;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.datafiles_list_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final DataLog obj = getItem(position);
            holder.data = obj;
            //noinspection ConstantConditions
            holder.date.setText(obj.getDateRetreived().toString());
            holder.location.setText("Location 1"); // TODO: hardcoded
            if(obj.isUploaded()){
                holder.status.setText("Uploaded");
            } else {
                holder.status.setText("Needs Upload");
            }
            /*
            if (inDeletionMode) {
                holder.deletedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            countersToDelete.add(obj.getCount());
                        } else {
                            countersToDelete.remove(obj.getCount());
                        }
                    }
                });
            } else {
                holder.deletedCheckBox.setOnCheckedChangeListener(null);
            }
            holder.deletedCheckBox.setVisibility(inDeletionMode ? View.VISIBLE : View.GONE);
            */
        }

        @Override
        public long getItemId(int index) {
            return getItem(index).getId();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView date;
            TextView location;
            TextView status;
            public DataLog data;

            MyViewHolder(View view) {
                super(view);
                date = (TextView) view.findViewById(R.id.date);
                location = (TextView) view.findViewById(R.id.location);
                status = (TextView) view.findViewById(R.id.status);
            }
        }
    }