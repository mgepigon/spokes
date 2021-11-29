package com.example.spokes.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.spokes.R;
import com.example.spokes.databinding.FragmentSummaryBinding;

/**
 * A placeholder fragment containing view for each tab clicked.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private FragmentSummaryBinding binding;

    //Makes the page when clicked or swiped (from SectionsPagerAdapter)
    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    //Happens before onCreateView, loads any savedInstance (which shouldn't happen)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSummaryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //Create View based on Index (Textviews used as filler to make sure it's working)
        switch(getArguments().getInt(ARG_SECTION_NUMBER)){
            case 1:
                //Textview of Distance, Time, Avg. Speed
                TextView summary = binding.summary;
                summary.setText("1");
                break;
            case 2:
                //Map Fragment of Route from Database
                TextView route = binding.route;
                route.setText("2");
                break;
            case 3:
                //Grab all trips from Database and display as ListView?
                TextView history = binding.history;
                history.setText("3");
                break;
        }
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}