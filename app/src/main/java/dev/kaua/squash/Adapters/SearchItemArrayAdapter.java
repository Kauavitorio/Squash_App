package dev.kaua.squash.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;

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
                if(people.getImageURL() == null || people.getImageURL().equals("default")) circleImageView.setImageResource(R.drawable.pumpkin_default_image);
                else Glide.with(context).load(people.getImageURL()).into(circleImageView);
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
            return EncryptHelper.decrypt(((DtoAccount) resultValue).getName_user());
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                suggestions.clear();
                for (DtoAccount people : tempItems) {
                    if (people.getName_user().toLowerCase().contains(constraint.toString().toLowerCase())) suggestions.add(people);
                    else if (people.getUsername().toLowerCase().contains(constraint.toString().toLowerCase())) suggestions.add(people);
                }
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                suggestions.clear();
                suggestions.addAll(tempItems);
                filterResults.count = suggestions.size();
                filterResults.values = suggestions;

            }
            return filterResults;
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
            }else
                notifyDataSetChanged();
        }
    };
}