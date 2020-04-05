package gods.game.levels;

import java.io.IOException;

import gods.base.GfxObject;
import gods.game.GodsLevel;

public class GodsGenericLevel extends GodsLevel 
{
	public GodsGenericLevel() throws IOException
	{
		super(null);
	}
	
	@Override
	protected boolean on_bonus_taken(GfxObject bonus) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void on_boss_death() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void on_button_pressed(String button_name) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void on_door_change_state(String door_name, boolean is_open) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void on_lever_activated(GfxObject lever,
			LeverActivationState state) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void on_object_drop(GfxObject bonus) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void on_object_picked_up(GfxObject object) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void on_room_entered(String room_door_name) {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean on_trigger_activated(String trigger_name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void on_world_restart(int world_count) {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected void on_level_loaded()
	{
	}


}
