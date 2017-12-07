package allen.g.utils;

import java.util.List;

/**
 * Created by local on 07/12/2017.
 */

public class ListFileCompareCallBack extends DiffUtil.Callback {
    private List<String> mOldList;
    private List<String> mNewList;

    public ListFileCompareCallBack(List<String> oldList, List<String> newList) {
        this.mOldList = oldList;
        this.mNewList = newList;
    }

    @Override
    public int getOldListSize() {
        return mOldList != null ? mOldList.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return mNewList != null ? mNewList.size() : 0;

    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mNewList.get(newItemPosition).equals(mOldList.get(oldItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mNewList.get(newItemPosition).equals(mOldList.get(oldItemPosition));
    }
}
