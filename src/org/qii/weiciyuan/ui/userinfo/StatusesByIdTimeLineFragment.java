package org.qii.weiciyuan.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.user.StatusesTimeLineDao;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IUserInfo;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;
import org.qii.weiciyuan.ui.maintimeline.AbstractTimeLineFragment;

import java.util.Map;
import java.util.Set;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 */
public class StatusesByIdTimeLineFragment extends AbstractTimeLineFragment {


    private UserBean userBean = new UserBean();


    private String token;
    private String uid;

    public StatusesByIdTimeLineFragment(String token, String uid) {
        this.token = token;
        this.uid = uid;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((AbstractAppActivity) getActivity()).getCommander();
        if (savedInstanceState != null && bean.getStatuses().size() == 0) {
            bean = (MessageListBean) savedInstanceState.getSerializable("bean");
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
        } else {
            refresh();

        }


    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        userBean = ((IUserInfo) getActivity()).getUser();
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("token", token);
        intent.putExtra("msg", bean.getStatuses().get(position));
        startActivity(intent);
    }


    protected void listViewFooterViewClick(View view) {
        if (!isBusying) {
            new TimeLineGetOlderMsgListTask().execute();
        }
    }


    public void refresh() {
        Map<String, AvatarBitmapWorkerTask> avatarBitmapWorkerTaskHashMap = ((AbstractAppActivity) getActivity()).getAvatarBitmapWorkerTaskHashMap();


        new TimeLineGetNewMsgListTask().execute();
        Set<String> keys = avatarBitmapWorkerTaskHashMap.keySet();
        for (String key : keys) {
            avatarBitmapWorkerTaskHashMap.get(key).cancel(true);
            avatarBitmapWorkerTaskHashMap.remove(key);
        }


    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.statusesbyidtimelinefragment_menu, menu);
        if (!TextUtils.isEmpty(userBean.getStatuses_count())) {
            String number = bean.getStatuses().size() + "/" + userBean.getStatuses_count();
            menu.findItem(R.id.statusesbyidtimelinefragment_status_number).setTitle(number);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.statusesbyidtimelinefragment_status_refresh:

                refresh();

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected MessageListBean getDoInBackgroundNewData() {
        StatusesTimeLineDao dao = new StatusesTimeLineDao(token, uid);

        if (getList().getStatuses().size() > 0) {
            dao.setSince_id(getList().getStatuses().get(0).getId());
        }
        MessageListBean result = dao.getGSONMsgList();

        return result;
    }

    @Override
    protected MessageListBean getDoInBackgroundOldData() {
        StatusesTimeLineDao dao = new StatusesTimeLineDao(token, uid);
        if (getList().getStatuses().size() > 0) {
            dao.setMax_id(getList().getStatuses().get(getList().getStatuses().size() - 1).getId());
        }
        MessageListBean result = dao.getGSONMsgList();

        return result;
    }

    @Override
    protected void afterGetNewMsg() {
        getActivity().invalidateOptionsMenu();
    }

    @Override
    protected void afterGetOldMsg() {
        getActivity().invalidateOptionsMenu();
    }
}


