package jg.rpg.msg.enterService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jg.rpg.common.PlayerMgr;
import jg.rpg.common.anotation.HandlerMsg;
import jg.rpg.common.protocol.MsgProtocol;
import jg.rpg.entity.MsgPacker;
import jg.rpg.entity.MsgUnPacker;
import jg.rpg.entity.Player;
import jg.rpg.entity.Session;
import jg.rpg.entity.msgEntity.ServerEntity;
import jg.rpg.exceptions.PlayerHandlerException;
import jg.rpg.msg.enterService.controller.EnterGameController;
import jg.rpg.utils.CommUtils;
import jg.rpg.utils.MsgUtils;

import org.apache.log4j.Logger;

public class EnterGameService {
	private Logger logger = Logger.getLogger(getClass());
	private EnterGameController egContoller;
	
	public EnterGameService(){
		egContoller = new EnterGameController();
	}

	@HandlerMsg(msgType = MsgProtocol.Login)
	public void LoginValidate(Session session , MsgUnPacker unpacker){
		logger.debug("LoginValidate");
		MsgPacker packer = new MsgPacker();
		try {
			String username = unpacker.popString();
			String pwd = unpacker.popString();
			Player player = egContoller.getUser(username ,pwd);
			if(player != null){
				Session _session = new Session();
				String sessionKey = CommUtils.generateSessionKey();
				_session.setSessionKey(sessionKey);
				_session.setCtx(session.getCtx());
				_session.setPlayer(player);
				PlayerMgr.getInstance().addPlayer(sessionKey, _session);
				
				packer.addInt(MsgProtocol.Success);
				packer.addString(sessionKey);
			}else{
				packer.addInt(MsgProtocol.Error);
				packer.addString("用户名与密码不匹配");
			}
			MsgUtils.sendMsg(session.getCtx(), packer);
			unpacker.close();
			
		} catch (Exception e) {
			logger.warn("handle user login error : "+e.getMessage());
		}

	}
	
	
	@HandlerMsg(msgType = MsgProtocol.Get_ServerList)
	public void GetServerList(Session session , MsgUnPacker unpacker){
		List<ServerEntity> servers = null;
		try {
			servers = egContoller.getServerList();
		} catch (SQLException e) {
			logger.error("read server List exception : " + e.getMessage());
		}
		if(servers == null || servers.isEmpty()){
			logger.warn("read server List empty : " + servers);
		}
		MsgPacker packer = new MsgPacker();
		try {
			packer.addInt(MsgProtocol.Success)
				.addInt(servers.size());
			for(ServerEntity server : servers){
				packer.addInt(server.getId())
					.addString(server.getName())
					.addString(server.getIp())
					.addInt(server.getCount());
			}
			MsgUtils.sendMsg(session.getCtx(), packer);
			unpacker.close();
		} catch (IOException e) {
			logger.error("send msg error : " + e.getMessage());
		}
	}
	
	@HandlerMsg(msgType = MsgProtocol.Register)
	public void registerPlayer(Session session ,MsgUnPacker unpacker ){
		logger.debug("registerPlayer");
		Player player = new Player();
		try {
			player.setUsername(unpacker.popString());
			player.setPwd(unpacker.popString());
			if(unpacker.hasNext())
				player.setPhoneNum(unpacker.popString());
			if(egContoller.registerPlayer(player) != null){
				MsgPacker packer = new MsgPacker();
				packer.addInt(MsgProtocol.Success);
				MsgUtils.sendMsg(session.getCtx(), packer);
			}else{
				throw new PlayerHandlerException("inset to tb_user error : "+player);
			}
		} catch (Exception e) {
			try {
				logger.warn("registerPlayer get data error : "+e.getMessage());
				MsgUtils.SendErroInfo(session.getCtx(), "信息输入错误,用户名已存在");
			} catch (IOException e1) {
				logger.error("server is fatal : "+e.getMessage());
			}
		}
	}
}
