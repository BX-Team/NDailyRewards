/**
 * NDailyRewards API Events
 * <p>
 * This package contains all the custom events fired by the NDailyRewards plugin.
 * These events allow other plugins to hook into the daily rewards system and
 * customize behavior, add additional functionality, or integrate with other systems.
 *
 * <h2>Available Events</h2>
 * <ul>
 *   <li>{@link org.bxteam.ndailyrewards.api.event.AutoClaimEvent} - Fired when rewards are automatically claimed</li>
 *   <li>{@link org.bxteam.ndailyrewards.api.event.PlayerClaimRewardEvent} - Fired when players manually claim rewards</li>
 *   <li>{@link org.bxteam.ndailyrewards.api.event.PlayerReceiveReminderEvent} - Fired when reminder notifications are sent</li>
 * </ul>
 *
 * <h2>Getting Started</h2>
 * <p>
 * To use these events in your plugin, add NDailyRewards as a dependency and
 * register event listeners using the standard Bukkit event system:
 *
 * <pre>{@code
 * @EventHandler
 * public void onAutoClaimEvent(AutoClaimEvent event) {
 *     Player player = event.getPlayer();
 *     int day = event.getDay();
 *     // Your custom logic here
 * }
 * }</pre>
 *
 * <h2>Documentation and Resources</h2>
 * <p>
 * For comprehensive documentation, setup guides, and examples, visit:
 * <ul>
 *   <li><a href="https://bxteam.org/docs/ndailyrewards/development/dependencies">Development Dependencies Guide</a></li>
 *   <li><a href="https://bxteam.org/docs/ndailyrewards/">Full Documentation</a></li>
 *   <li><a href="https://github.com/BX-Team/NDailyRewards">GitHub Repository</a></li>
 * </ul>
 *
 * <h2>Support</h2>
 * <p>
 * For support, bug reports, or feature requests:
 * <ul>
 *   <li>Join our <a href="https://discord.gg/qNyybSSPm5">Discord Server</a></li>
 *   <li>Create an issue on <a href="https://github.com/BX-Team/NDailyRewards/issues">GitHub</a></li>
 * </ul>
 *
 * @version 3.3.0
 * @since 3.0.0
 */
package org.bxteam.ndailyrewards.api.event;

