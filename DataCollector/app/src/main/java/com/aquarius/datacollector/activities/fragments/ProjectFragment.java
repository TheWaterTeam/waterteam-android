package com.aquarius.datacollector.activities.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aquarius.datacollector.R;
import com.aquarius.datacollector.activities.adapters.DataLoggerRecyclerViewAdapter;
import com.aquarius.datacollector.api.Api;
import com.aquarius.datacollector.api.ErrorMessageException;
import com.aquarius.datacollector.database.DataLog;
import com.aquarius.datacollector.database.DataLogger;
import com.aquarius.datacollector.database.Project;
import com.aquarius.datacollector.dummy.DummyContent;

import java.io.IOException;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by matthewxi on 11/13/17.
 */

// For the moment this is the fragment that lists all devices
public class ProjectFragment extends Fragment {

    private static final String TAG = "ProjectFragment";

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 2;
    private DataFilesFragment.OnListFragmentInteractionListener mListener;

    private Realm realm;

    private RecyclerView mRecyclerView;


        /**
         * Mandatory empty constructor for the fragment manager to instantiate the
         * fragment (e.g. upon screen orientation changes).
         */
    public ProjectFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem upload = menu.add("Switch Project");
        upload.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    Api.getInstance().getProjects(getContext(), new Callback<List<Project>>() {
                        @Override
                        public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                            // show a multi list to select from
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //TODO get the projects
                                    CharSequence colors[] = new CharSequence[] {"red", "green", "blue", "black"};

                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setTitle("Pick a color");
                                    builder.setItems(colors, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // the user clicked on colors[which]
                                            // todo set the selected project
                                        }
                                    });
                                    builder.show();
                                }
                            });

                        }

                        @Override
                        public void onFailure(Call<List<Project>> call, Throwable t) {
                            Toast.makeText(getContext(), "Problem contacting server", Toast.LENGTH_SHORT);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ErrorMessageException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dataloggers_fragment, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            refreshDataLoggersList();
        }
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // This is not a sync

            Api.getInstance().getDataloggers(getContext(), 5, new Callback<List<DataLogger>>() {
                @Override
                public void onResponse(Call<List<DataLogger>> call, Response<List<DataLogger>> response) {
                    List<DataLogger> dataloggers = response.body();
                    realm.beginTransaction();
                    realm.delete(DataLogger.class);
                    realm.copyToRealm(dataloggers);
                    realm.commitTransaction();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshDataLoggersList();
                        }
                    });
                }

                @Override
                public void onFailure(Call<List<DataLogger>> call, Throwable t) {
                    Log.d(TAG, "No Good");
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ErrorMessageException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
        */
    }

    @Override
    public void onResume() {
        super.onResume();



    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DummyContent.DummyItem item);
    }

    private void refreshDataLoggersList() {

        //TODO: Query from Realm here.
        RealmQuery<DataLogger> query = realm.where(DataLogger.class);
        RealmResults<DataLogger> results = query.findAll();

        // TODO use the Realm adapter
        if(mRecyclerView != null){
            mRecyclerView.setAdapter(new DataLoggerRecyclerViewAdapter(results)); //, mListener));
        }

    }

}
