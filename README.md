# Real World Flight (AiT add-on)

Personal add-on for Adventures in Time (AIT Forger), Forge 1.20.1.

AiT already contains real world flight: the TARDIS exterior becomes a vehicle that you ride and fly
around the overworld. It ships switched off, and only creative players can start it. This add-on:

- switches it on (sets AiT's own `rwfEnabled` at server start and on login), and
- lets survival players start it from the console's visualiser control.

## How to fly

1. The TARDIS must be landed, the doors closed, and the gravitational subsystem on.
2. Right-click (or hit) the visualiser control on the console. You are put on the TARDIS outside.
3. W/A/S/D to move, Space to rise, Shift to descend. AiT scales your speed by the TARDIS throttle,
   so by default the add-on raises a throttle of 0 to 1 when you take off.
4. To land, sneak while on the ground (or with anti-gravs on) in a safe spot. You are returned inside.

Flying drains the gravitational subsystem's durability, and the TARDIS needs power.

## Config (`realflight-common.toml`)

- `enableRealWorldFlight` (true): switch AiT's real world flight on.
- `allowSurvival` (true): let non-creative players start it.
- `minimumThrottleOnTakeoff` (1): raise the throttle to this on take-off (0 = leave it alone).

Creative players and the `/ait flight` command keep working exactly as in AiT.

## Building

Same as the other add-ons: put the folder on GitHub (including the hidden `.github` folder) and the
Actions workflow builds `build/libs/*.jar`. No AiT jar is needed to build; the add-on talks to AiT
through reflection.
