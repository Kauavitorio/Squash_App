package dev.kaua.squash.Activitys.Setting.AccountSetting.Lang.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import dev.kaua.squash.Activitys.Setting.AccountSetting.Lang.DtoLang;
import dev.kaua.squash.Activitys.Setting.LocaleHelper;
import dev.kaua.squash.R;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {

    private final Activity mContext;
    private final List<DtoLang> mLanguageList;

    public LanguageAdapter(Activity mContext, List<DtoLang> mLanguageList){
        this.mContext = mContext;
        this.mLanguageList = mLanguageList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_language_selector, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull @NotNull LanguageAdapter.ViewHolder holder, int position) {
        final DtoLang language = mLanguageList.get(position);
        if(language != null){
            if(LocaleHelper.getCurrentLanguage(mContext).equals(language.getName())) holder.selected.setVisibility(View.VISIBLE);
            holder.display.setText(language.getDisplay());
            holder.icon.setImageDrawable(mContext.getDrawable(language.getIcon()));

            holder.itemView.setOnClickListener(v -> {
                holder.itemView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.click_anim));
                if(!LocaleHelper.getCurrentLanguage(mContext).equals(language.getName()))
                    LocaleHelper.setLocale(mContext, language.getName());
            });
        }
    }


    @Override
    public int getItemCount() { return mLanguageList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView display;
        private final ImageView icon, selected;

        @SuppressLint("CutPasteId")
        public ViewHolder(View itemView){
            super(itemView);
            display = itemView.findViewById(R.id.language);
            icon = itemView.findViewById(R.id.language_flag);
            selected = itemView.findViewById(R.id.ic_selected_lang);
        }
    }

}
