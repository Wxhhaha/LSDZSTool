package com.lsdzs.lsdzs_tool.ui.settings;

import android.content.Context;

import com.lsdzs.lsdzs_tool.R;
import com.lsdzs.lsdzs_tool.ble.ParamModel;
import com.lsdzs.lsdzs_tool.databinding.ParamValueItemLayoutBinding;
import com.wxh.basiclib.adapter.BaseBindingAdapter;

public class ParamValueAdapter extends BaseBindingAdapter<ParamModel, ParamValueItemLayoutBinding> {
    private ItemClick itemClick;

    public ItemClick getItemClick() {
        return itemClick;
    }

    public void setItemClick(ItemClick itemClick) {
        this.itemClick = itemClick;
    }

    public ParamValueAdapter(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId(int i) {
        return R.layout.param_value_item_layout;
    }

    @Override
    protected void onBindItem(ParamValueItemLayoutBinding paramValueItemLayoutBinding, ParamModel paramModel,int position) {
        paramValueItemLayoutBinding.tvName.setText(paramModel.getName());
        paramValueItemLayoutBinding.tvValue.setText(paramModel.getValue()+"");
        paramValueItemLayoutBinding.getRoot().setOnClickListener(view -> {
            itemClick.onItemClickeListener(paramModel);
        });
    }

    public interface ItemClick{
        void onItemClickeListener(ParamModel model);
    }
}
