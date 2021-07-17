package dev.kaua.squash.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.R;

@SuppressWarnings("unchecked")
public class SearchItemArrayAdapter extends ArrayAdapter<DtoAccount> {

    Context context;
    int resource, textViewResourceId;
    List<DtoAccount> items, tempItems, suggestions;

    public SearchItemArrayAdapter(Context context, int resource, int textViewResourceId, List<DtoAccount> items) {
        super(context, resource, textViewResourceId, items);
        this.context = context;
        this.resource = resource;
        this.textViewResourceId = textViewResourceId;
        this.items = items;
        tempItems = new ArrayList<>(items); // this makes the difference.
        suggestions = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.adapter_search_layout, parent, false);
        }
        DtoAccount people = items.get(position);
        if (people != null) {
            TextView lblName = (TextView) view.findViewById(R.id.txt_user_name_search);
            CircleImageView circleImageView = view.findViewById(R.id.ic_user_image);
            if (lblName != null){
                Picasso.get().load(people.getProfile_image()).into(circleImageView);
                lblName.setText(people.getName_user());
            }
        }
        return view;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    /**
     * Custom Filter implementation for custom suggestions we provide.
     */
    Filter nameFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((DtoAccount) resultValue).getName_user();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (DtoAccount people : tempItems) {
                    if (people.getName_user().toLowerCase().contains(constraint.toString().toLowerCase())) suggestions.add(people);
                    else if (people.getUsername().toLowerCase().contains(constraint.toString().toLowerCase())) suggestions.add(people);
                    else if (people.getJoined_date().toLowerCase().contains(constraint.toString().toLowerCase())) suggestions.add(people);
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else
                return new FilterResults();
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<DtoAccount> filterList = (ArrayList<DtoAccount>) results.values;
            if (results.count > 0) {
                clear();
                for (DtoAccount people : filterList) {
                    add(people);
                    notifyDataSetChanged();
                }
            }
        }
    };
}