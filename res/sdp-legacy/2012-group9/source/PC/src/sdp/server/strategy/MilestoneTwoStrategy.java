package sdp.server.strategy;

/*
import javax.swing.JButton;

import sdp.server.PcServer;
import sdp.server.gui.ControlPanel;
import sdp.server.math.Vector2;
import sdp.server.strategy.StrategyGoal.Type;
*/

public class MilestoneTwoStrategy {
	/*
	enum Mode {
		STANDBY, DRIBBLE, NAVIGATE, DONE, GO_FORWARD
	}

	private Mode currentMode = Mode.STANDBY;

	public MilestoneTwoStrategy(PcServer server) {
		super(server);

		this.panel.addButton("Execute Dribble",
				new ControlPanel.ButtonCallback() {

					@Override
					public void buttonClicked(JButton button) {

						MilestoneTwoStrategy.this.startDribble();

					}
				});
		this.panel.addButton("Go to Ball", new ControlPanel.ButtonCallback() {

			@Override
			public void buttonClicked(JButton button) {

				MilestoneTwoStrategy.this.goToBall();

			}
		});
		this.panel.addButton("Go Forward", new ControlPanel.ButtonCallback() {

			@Override
			public void buttonClicked(JButton button) {

				MilestoneTwoStrategy.this.goForward();

			}
		});

	}

	public void startDribble() {
		this.disable();
		this.currentMode = Mode.DRIBBLE;
		this.enable();
	}

	public void goToBall() {
		this.disable();
		this.currentMode = Mode.NAVIGATE;
		this.enable();
	}
	public void goForward() {
		this.disable();
		this.currentMode = Mode.GO_FORWARD;
		this.enable();
	}


	@Override
	public void enable() {
		super.enable();
		// this.currentMode = Mode.STANDBY;
		this.ctrl.setStationary(true);
	}

	@Override
	public void disable() {
		super.disable();
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		// this.isBallInRange();
		if (!this.enabled) {
			return;
		}

		if (this.currentMode == Mode.DRIBBLE) {

			// Wait 5 seconds before deciding on the goal
			// if ((System.currentTimeMillis() - this.initialTick) > 1000) {

			Vector2 ballPosition = this.world.getBallState().getPosition();

			Vector2 targetDribble = (this.world.getBallState().getPosition())
					.minus(this.ctrl.getRobotState().getPosition())
					.computeUnit();

			double ballRotation = targetDribble.computeAngle();

			this.enqueueGoal(new StrategyGoal(Type.GOTO, this.ctrl.getRobotState().getPosition(), ballRotation, 0.5));

			Vector2 targetPosition = new Vector2(ballPosition.getX()
					+ Math.cos(Math.PI + ballRotation) * 0.05,
					ballPosition.getY() + Math.sin(Math.PI + ballRotation)
							* 0.05);

			this.enqueueGoal(new StrategyGoal(Type.GOTO, targetPosition, 0.25));
			this.enqueueGoal(new StrategyGoal(Type.GOTO, ballPosition
					.plus(targetDribble.times(1.0)), 1.0));

			this.runCurrentGoal();
			this.currentMode = Mode.DONE;

		} else if (this.currentMode == Mode.NAVIGATE) {
			Vector2 ballPosition = this.world.getBallState().getPosition();
			this.enqueueGoal(new StrategyGoal(Type.GOTO, ballPosition, 0.9));
			this.runCurrentGoal();
			this.currentMode = Mode.DONE;
		} else if( this.currentMode == Mode.GO_FORWARD ){
			this.enqueueGoal(new StrategyGoal(Type.GOTO, this.ctrl.getRobotState().getPosition().plus( Vector2.fromAngle( this.ctrl.getRobotState().getRotation() ).times(1.0) ), 1.0));
			this.runCurrentGoal();
			this.currentMode = Mode.DONE;
		}

		super.update();
		
		//System.out.println(this.ctrl.);

	}
	*/

}
