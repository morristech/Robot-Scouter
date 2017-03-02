package com.supercilex.robotscouter.ui.teamlist;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import com.firebase.ui.database.ChangeEventListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.supercilex.robotscouter.R;
import com.supercilex.robotscouter.data.model.Team;
import com.supercilex.robotscouter.data.util.TeamHelper;
import com.supercilex.robotscouter.util.Constants;
import com.supercilex.robotscouter.util.FirebaseAdapterHelper;

import java.util.List;

public class TeamListAdapter extends FirebaseRecyclerAdapter<Team, TeamViewHolder> {
    private Fragment mFragment;
    private View mNoTeamsImage;
    private View mNoTeamsText;

    private TeamMenuManager mMenuManager;

    public TeamListAdapter(Fragment fragment, TeamMenuManager menuManager) {
        super(Constants.sFirebaseTeams,
              Team.class,
              R.layout.team_list_row_layout,
              TeamViewHolder.class);
        mFragment = fragment;
        mMenuManager = menuManager;

        View view = mFragment.getView();
        mNoTeamsImage = view.findViewById(R.id.no_teams_icon);
        mNoTeamsText = view.findViewById(R.id.no_teams_text);
    }

    @Override
    public void populateViewHolder(TeamViewHolder teamHolder, Team team, int position) {
        team.getHelper().fetchLatestData(mFragment.getContext());
        teamHolder.bind(team,
                        mMenuManager,
                        mMenuManager.getSelectedTeams().contains(team.getHelper()),
                        !mMenuManager.getSelectedTeams().isEmpty());

        showNoTeamsHint(getItemCount() == 0);
    }

    @Override
    public void onChildChanged(ChangeEventListener.EventType type,
                               DataSnapshot snapshot,
                               int index,
                               int oldIndex) {
        showNoTeamsHint(getItemCount() == 0);

        switch (type) {
            case CHANGED:
            case MOVED:
                for (TeamHelper oldTeam : mMenuManager.getSelectedTeams()) {
                    Team team = getItem(index);
                    if (TextUtils.equals(oldTeam.getTeam().getKey(), team.getKey())) {
                        mMenuManager.onSelectedTeamMoved(oldTeam, team.getHelper());
                        break;
                    }
                }
                break;
            case REMOVED:
                if (!mMenuManager.getSelectedTeams().isEmpty()) {
                    List<Team> tmpTeams = FirebaseAdapterHelper.getItems(this);
                    for (TeamHelper oldTeamHelper : mMenuManager.getSelectedTeams()) {
                        if (!tmpTeams.contains(oldTeamHelper.getTeam())) { // We found the deleted item
                            mMenuManager.onSelectedTeamChanged(oldTeamHelper);
                            break;
                        }
                    }
                }
                break;
        }
        super.onChildChanged(type, snapshot, index, oldIndex);
    }

    @Override
    public void onCancelled(DatabaseError error) {
        FirebaseCrash.report(error.toException());
    }

    private void showNoTeamsHint(boolean isVisible) {
        if (mNoTeamsImage == null || mNoTeamsText == null) return;
        mNoTeamsImage.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        mNoTeamsText.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}
