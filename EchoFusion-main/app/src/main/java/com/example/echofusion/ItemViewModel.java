package com.example.echofusion;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.Set;
import java.util.logging.Filter;

public class ItemViewModel extends ViewModel {
    private final MutableLiveData<String> selectedItem = new MutableLiveData<String>();
    public void selectItem(String item) {
        selectedItem.setValue(item);
    }
    public LiveData<String> getSelectedItem() {
        return selectedItem;
    }
}


//example for sendig filters back and forth in a list
//public class ListViewModel extends ViewModel {
//
//    private final MutableLiveData<Set<Filter>> filters = new MutableLiveData<>();
//
//    private final LiveData<List<String>> originalList = ...;
//    private final LiveData<List<String>> filteredList = ...;
//
//    public LiveData<List<String>> getFilteredList() {
//        return filteredList;
//    }
//
//    public LiveData<Set<Filter>> getFilters() {
//        return filters;
//    }
//
//    public void addFilter(Filter filter) { ... }
//
//    public void removeFilter(Filter filter) { ... }
//}
//
//public class ListFragment extends Fragment {
//    private ListViewModel viewModel;
//
//    @Override
//    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        viewModel = new ViewModelProvider(requireActivity()).get(ListViewModel.class);
//        viewModel.getFilteredList().observe(getViewLifecycleOwner(), list -> {
//            // Update the list UI.
//        });
//    }
//}
//
//public class FilterFragment extends Fragment {
//    private ListViewModel viewModel;
//
//    @Override
//    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
//        viewModel = new ViewModelProvider(requireActivity()).get(ListViewModel.class);
//        viewModel.getFilters().observe(getViewLifecycleOwner(), set -> {
//            // Update the selected filters UI.
//        });
//    }
//
//    public void onFilterSelected(Filter filter) {
//        viewModel.addFilter(filter);
//    }
//
//    public void onFilterDeselected(Filter filter) {
//        viewModel.removeFilter(filter);
//    }
//}
