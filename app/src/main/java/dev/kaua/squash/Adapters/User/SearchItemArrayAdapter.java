package dev.kaua.squash.Adapters.User;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.kaua.squash.Data.Account.DtoAccount;
import dev.kaua.squash.LocalDataBase.DaoFollowing;
import dev.kaua.squash.R;
import dev.kaua.squash.Security.EncryptHelper;
import dev.kaua.squash.Tools.MyPrefs;

@SuppressWarnings("unchecked")
public class SearchItemArrayAdapter extends ArrayAdapter<DtoAccount> {

    Context context;
    int resource, textViewResourceId;
    List<DtoAccount> items, tempItems;
    final List<DtoAccount> suggestions = new ArrayList<>();
    DaoFollowing daoFollowing;

    public SearchItemArrayAdapter(Context context, int resource, int textViewResourceId, List<DtoAccount> items) {
        super(context, resource, textViewResourceId, items);
        this.context = context;
        this.resource = resource;
        this.textViewResourceId = textViewResourceId;
        this.items = items;
        daoFollowing = new DaoFollowing(context);
        tempItems = new ArrayList<>(items); // this makes the difference.
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.adapter_search_layout, parent, false);
        }
        final DtoAccount people = items.get(position);
        if (people != null && people.getAccount_id_cry() != null && EncryptHelper.decrypt(people.getAccount_id_cry()) != null) {
            final long user_id = Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(people.getAccount_id_cry())));
            if(user_id > DtoAccount.NORMAL_ACCOUNT){
                final TextView lblName = view.findViewById(R.id.txt_user_name_search);
                final TextView txt_name_search = view.findViewById(R.id.txt_name_search);
                final ImageView ic_account_badge_search = view.findViewById(R.id.ic_account_badge_search);
                final CircleImageView circleImageView = view.findViewById(R.id.ic_user_image);
                if (lblName != null && ic_account_badge_search != null){
                    if(people.getImageURL() == null || people.getImageURL().equals(DtoAccount.DEFAULT)) circleImageView.setImageResource(R.drawable.pumpkin_default_image);
                    else Glide.with(context).load(people.getImageURL()).into(circleImageView);
                    lblName.setText(people.getUsername());
                    txt_name_search.setText(people.getName_user());

                    //  Some user that doesn't have level have been get badge
                    /*if(people.getVerification_level() != null && EncryptHelper.decrypt(people.getVerification_level()) != null){
                        final long user_level = Long.parseLong(Objects.requireNonNull(EncryptHelper.decrypt(people.getVerification_level())));
                        if(user_level > DtoAccount.NORMAL_ACCOUNT){
                            ic_account_badge_search.setVisibility(View.VISIBLE);
                            if (user_level == DtoAccount.VERIFY_ACCOUNT)
                                ic_account_badge_search.setImageDrawable(context.getDrawable(R.drawable.ic_verified_account));
                            else
                                ic_account_badge_search.setImageDrawable(context.getDrawable(R.drawable.ic_verified_employee_account));
                        }
                    }*/

                    if(daoFollowing.check_if_follow(MyPrefs.getUserInformation(context).getAccount_id(), user_id))
                        view.findViewById(R.id.txt_following_search).setVisibility(View.VISIBLE);
                    else
                        view.findViewById(R.id.txt_following_search).setVisibility(View.GONE);

                }
            }else
                view.findViewById(R.id.txt_following_search).setVisibility(View.GONE);
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
    final Filter nameFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return EncryptHelper.decrypt(((DtoAccount) resultValue).getName_user());
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                suggestions.clear();
                constraint = constraint.toString().replace("@", "");
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