# Real World Flight (AiT add-on)

Personal add-on for Adventures in Time (AIT Forger), Forge 1.20.1.

AiT already contains real world flight: the TARDIS exterior becomes a vehicle that you ride and fly
around the overworld. It ships switched off, and only creative players can start it. This add-on:

- switches it on (sets AiT's own `rwfEnabled` at server start and on login),
- adds a `/realflight` command to take off from inside any TARDIS, and
- lets survival players use the console's visualiser control on consoles that have one.

## How to fly

1. The TARDIS must be landed and the gravitational subsystem on.
2. Type `/realflight` while inside your TARDIS (works for everyone, and on every console, including the
   Toyota console, which has no visualiser control). Or, on consoles that have one, right-click (or hit)
   the visualiser control. The doors are closed for you and you are put on the TARDIS outside.
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
