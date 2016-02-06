﻿using UnityEngine;
using System.Collections;
using SimpleFramework;
using SimpleFramework.Manager;

public class StartUpCommand : ControllerCommand {

    public override void Execute(IMessage message) {
        if (!Util.CheckEnvironment()) return;

        GameObject gameMgr = GameObject.Find("GlobalGenerator");
        if (gameMgr != null) {
            AppView appView = gameMgr.AddComponent<AppView>();
        }
        //-----------------关联命令-----------------------
        //AppFacade.Instance.RegisterCommand(NotiConst.DISPATCH_MESSAGE, typeof(SocketCommand));

        ////-----------------初始化管理器-----------------------
        //AppFacade.Instance.AddManager(ManagerName.Lua, new LuaScriptMgr());

        //AppFacade.Instance.AddManager<PanelManager>(ManagerName.Panel);
        AppFacade.Instance.AddManager(ManagerName.Music, AudioManager.Instance);
        AppFacade.Instance.AddManager(ManagerName.Lua, LuaManager.Instance);
        AppFacade.Instance.AddManager(ManagerName.Resource,ResourceManager.Instance);
        //AppFacade.Instance.AddManager<TimerManager>(ManagerName.Timer);
        //AppFacade.Instance.AddManager<NetworkManager>(ManagerName.Network);
        //AppFacade.Instance.AddManager<ThreadManager>(ManagerName.Thread);
        //AppFacade.Instance.AddManager<GameManager>(ManagerName.Game);

        Debug.Log("SimpleFramework StartUp-------->>>>>");
    }
}