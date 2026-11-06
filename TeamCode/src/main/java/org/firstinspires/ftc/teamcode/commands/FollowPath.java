package org.firstinspires.ftc.teamcode.commands;

import android.util.Log;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.arcrobotics.ftclib.command.CommandBase;

public class FollowPath extends CommandBase {

    private final com.pedropathing.follower.Follower m_follower;

    private com.pedropathing.pathgen.Path m_path;

    private com.pedropathing.pathgen.PathChain m_pathChain;
    private boolean holdEnd = false;

    private long waitUntil;

    public FollowPath(com.pedropathing.follower.Follower follower, com.pedropathing.pathgen.Path path, boolean holdEnd) {
        this.m_follower = follower;
        this.m_path = path;
        this.holdEnd = holdEnd;
        Log.d("FollowPath", "constructor");
    }

    public FollowPath(com.pedropathing.follower.Follower follower, com.pedropathing.pathgen.Path path) {
        this.m_follower = follower;
        this.m_path = path;
    }

    public FollowPath(com.pedropathing.follower.Follower follower, com.pedropathing.pathgen.PathChain pathChain, boolean holdEnd) {
        this.m_follower = follower;
        this.m_pathChain = pathChain;
        this.holdEnd = holdEnd;
    }

    public FollowPath(com.pedropathing.follower.Follower follower, com.pedropathing.pathgen.PathChain pathChain) {
        this.m_follower = follower;
        this.m_pathChain = pathChain;
    }

    @Override
    public void initialize() {
        if (m_path != null) {
            Log.d("Follower", "initialized");
            m_follower.followPath(m_path, holdEnd);
        } else if (m_pathChain != null) {
            Log.d("Follower", "initialized");
            m_follower.followPath(m_pathChain, holdEnd);
        }
    }

    @Override
    public void execute() {
        // THIS IS COMMENTED OUT BECAUSE THE AUTO ALREADY CONSTANTLY RUNS THIS IN THE MAIN LOOP
        //m_follower.drawOnDashBoard();
        //m_follower.update();
    }

    @Override
    public boolean isFinished() {
        if (!m_follower.isBusy()) {
            Log.d("Follower", "finished");
        }
        return !m_follower.isBusy();
    }
}
