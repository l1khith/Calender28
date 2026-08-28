
---

# FEATURE.md
## Calender28 — Complete Coin & Rewards System Specification

**Version:** 1.0  
**Date:** August 28, 2026  
**Status:** Ready for Implementation  
**Target:** Shipaton 2026 Next Gen Submission  
**Author:** Calender28 Development Team

---

## 1. Overview

The Coin System is a gamification layer that rewards users for consistent habit tracking and task completion. Users earn **CalCoins** through daily engagement, which can be redeemed to unlock Premium features. This system increases user retention, encourages daily app usage, and provides a non-monetary path to premium.

---

## 2. Coin Economy — Complete Specifications

### 2.1 Basic Parameters

| Parameter | Value | Notes |
|-----------|-------|-------|
| **Currency Name** | CalCoins | Displayed as "🪙 CalCoins" in UI |
| **Starting Balance** | 0 | New users start with zero coins |
| **Maximum Balance** | Unlimited | No cap on total coins |
| **Storage** | Local-only (Room DB) | No cloud sync; coins persist locally |
| **Analytics** | Track all coin events | For user engagement insights |

### 2.2 Reward Rules — Complete Table

| Action | Coins Earned | Frequency | Trigger Condition |
|--------|--------------|-----------|-------------------|
| **Daily App Open** | +1 | Once per day | First app launch of the day (resets at midnight) |
| **Habit Cycle Completion** | +10 | Per cycle per habit | User marks the habit cycle as complete |
| **Partial Habit Progress** | +1 | Per day tracked | Each day logged within a habit cycle |
| **Recurring Task Completion** | +2 | Per task per day | User checks off a recurring task |
| **7-Day Streak Bonus** | +50 | Once per 7-day streak | 7 continuous days of same recurring task |
| **14-Day Streak Bonus** | +100 | Once per 14-day streak | 14 continuous days of same recurring task |
| **30-Day Streak Bonus** | +200 | Once per 30-day streak | 30 continuous days of same recurring task |
| **Milestone: 10 Cycles** | +100 | One-time | User completes 10 habit cycles total |
| **Milestone: 50 Cycles** | +500 | One-time | User completes 50 habit cycles total |
| **Milestone: 100 Cycles** | +1000 | One-time | User completes 100 habit cycles total |
| **Promo Code** | Variable | One-time per code | User enters a valid promo code |

### 2.3 Spending Options

| Item | Coin Cost | Description |
|------|-----------|-------------|
| **Premium Unlock** | 500 | One-time purchase; unlocks ad-free experience |
| **Additional Features** | N/A | No other spending options currently |

### 2.4 Premium Benefits

When a user unlocks Premium (either via coins or subscription):

| Feature | Status |
|---------|--------|
| **Ad-Free Experience** | ✅ All ads removed |
| **Unlimited Habits** | ✅ No limit on number of habits |
| **Dark Mode** | ✅ Available in settings |
| **Advanced Stats** | ✅ Detailed progress analytics |
| **Data Export** | ✅ Export habit data as CSV |
| **Custom Themes** | ✅ Choose from multiple color themes |
| **Priority Support** | ✅ Faster email support |

---

## 3. User Experience Specifications

### 3.1 Coin Display Location

| Location | Element | Description |
|----------|---------|-------------|
| **Home Screen** | Top Bar, Left of Sync Button | Shows current coin balance with coin icon |
| **Full-Screen Store** | When clicking coin icon | Detailed view with balance, transaction history, and premium purchase option |

### 3.2 Coin Animations

| Event | Animation | Description |
|-------|-----------|-------------|
| **Coins Earned** | Floating "+X" Text | Green text floats upward and fades out |
| **Coins Earned** | Coin Flip Animation | Small coin icon flips 180° with sparkle effect |
| **Premium Purchased** | Celebration | Confetti or fireworks effect |
| **Streak Bonus** | Enhanced Animation | Larger "+50" with golden glow effect |

### 3.3 Notifications

| Event | Notification Type | Message |
|-------|-------------------|---------|
| **Coins Earned** | In-App Snackbar | "🪙 +10 CalCoins earned!" |
| **Streak Bonus** | In-App Snackbar | "🔥 +50 CalCoins for 7-day streak!" |
| **Daily Reward** | In-App Snackbar | "☀️ +1 CalCoin for opening app!" |
| **Premium Unlocked** | Dialog + Snackbar | "🎉 Premium unlocked successfully!" |
| **Insufficient Coins** | Snackbar | "⚠️ Not enough CalCoins. Need 500." |

### 3.4 Store Screen UI

The store opens as a **full-screen page** when the user clicks the coin icon in the top bar.

**Layout (Top to Bottom):**

1. **Header**: "🪙 Coin Store" with back arrow
2. **Balance Card**: Displays current balance prominently
3. **Transaction History**: Scrollable list of recent transactions
4. **Premium Unlock Section**:
   - "✨ Unlock Premium" title
   - List of premium benefits
   - "Unlock for 500 CalCoins" button
   - "Or subscribe via Google Play" button (RevenueCat)

---

## 4. Technical Specifications

### 4.1 Database Schema

**CoinBalanceEntity**
| Column | Type | Description |
|--------|------|-------------|
| `id` | INT (PK) | Always 1 |
| `balance` | INT | Current CalCoin balance |

**CoinTransactionEntity**
| Column | Type | Description |
|--------|------|-------------|
| `id` | TEXT (PK) | UUID |
| `amount` | INT | Positive = earned, Negative = spent |
| `reason` | TEXT | See reason enum below |
| `timestamp` | TEXT | ISO 8601 timestamp |

**Transaction Reasons Enum**:
- `DAILY_LOGIN`
- `HABIT_CYCLE_COMPLETE`
- `PARTIAL_HABIT_PROGRESS`
- `DAILY_TASK`
- `STREAK_7_DAY`
- `STREAK_14_DAY`
- `STREAK_30_DAY`
- `MILESTONE_10_CYCLES`
- `MILESTONE_50_CYCLES`
- `MILESTONE_100_CYCLES`
- `PREMIUM_PURCHASE`
- `PROMO_CODE`

### 4.2 Coin Logic Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                        USER ACTION                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐    ┌─────────────────┐    ┌───────────────┐  │
│  │ Open App    │───▶│ Daily Login     │───▶│ Add +1 Coin   │  │
│  │ (First time │    │ Check           │    │               │  │
│  │  of day)    │    │                 │    │               │  │
│  └─────────────┘    └─────────────────┘    └───────────────┘  │
│                                                                 │
│  ┌─────────────┐    ┌─────────────────┐    ┌───────────────┐  │
│  │ Complete    │───▶│ Habit Cycle     │───▶│ Add +10 Coins │  │
│  │ Habit Cycle │    │ Check           │    │               │  │
│  └─────────────┘    └─────────────────┘    └───────────────┘  │
│                                                                 │
│  ┌─────────────┐    ┌─────────────────┐    ┌───────────────┐  │
│  │ Log Day in  │───▶│ Partial Habit   │───▶│ Add +1 Coin   │  │
│  │ Habit       │    │ Progress        │    │               │  │
│  └─────────────┘    └─────────────────┘    └───────────────┘  │
│                                                                 │
│  ┌─────────────┐    ┌─────────────────┐    ┌───────────────┐  │
│  │ Check Off   │───▶│ Recurring Task  │───▶│ Add +2 Coins  │  │
│  │ Daily Task  │    │ Check           │    │               │  │
│  └─────────────┘    └─────────────────┘    └───────────────┘  │
│                              │                                  │
│                              ▼                                  │
│                    ┌─────────────────┐    ┌───────────────┐  │
│                    │ Check Streak    │───▶│ Add +50/100/  │  │
│                    │ (7/14/30 days)  │    │ 200 Coins     │  │
│                    └─────────────────┘    └───────────────┘  │
│                                                                 │
│  ┌─────────────┐    ┌─────────────────┐    ┌───────────────┐  │
│  │ Milestone   │───▶│ Check Cycle     │───▶│ Add +100/500/│  │
│  │ Reached     │    │ Count           │    │ 1000 Coins    │  │
│  └─────────────┘    └─────────────────┘    └───────────────┘  │
│                                                                 │
│  ┌─────────────┐    ┌─────────────────┐    ┌───────────────┐  │
│  │ Premium     │───▶│ Check Balance   │───▶│ Deduct 500    │  │
│  │ Purchase    │    │ >= 500 coins    │    │ Coins          │  │
│  └─────────────┘    └─────────────────┘    └───────────────┘  │
│                              │                                  │
│                              ▼                                  │
│                    ┌─────────────────┐                         │
│                    │ Call RevenueCat │                         │
│                    │ API to Grant    │                         │
│                    │ Entitlement     │                         │
│                    └─────────────────┘                         │
└─────────────────────────────────────────────────────────────────┘
```

### 4.3 Streak Logic

| Day | Status | Action |
|-----|--------|--------|
| Day 1-6 | ✅ Completed | +2 coins per day |
| Day 7 | ✅ Completed | +2 coins + **+50 bonus** = 52 coins |
| Day 8-13 | ✅ Completed | +2 coins per day |
| Day 14 | ✅ Completed | +2 coins + **+100 bonus** = 102 coins |
| Day 15-29 | ✅ Completed | +2 coins per day |
| Day 30 | ✅ Completed | +2 coins + **+200 bonus** = 202 coins |
| Any Missed Day | ❌ Missed | Streak resets to 0, no bonus coins |

### 4.4 Edge Cases & Error Handling

| Scenario | Handling |
|----------|----------|
| **Insufficient Coins** | Show snackbar: "⚠️ Not enough CalCoins. Need 500." |
| **Duplicate Reward** | Check if reward already granted today (e.g., daily login) |
| **Offline Mode** | All rewards work offline; no sync needed |
| **App Data Cleared** | Coins lost; no recovery (local-only) |
| **Reinstall App** | Coins lost; no recovery (local-only) |
| **Premium Already Owned** | Disable purchase button, show "Already Premium" |
| **Invalid Promo Code** | Show error: "❌ Invalid promo code" |
| **RevenueCat API Failure** | Show error: "⚠️ Failed to grant premium. Please try again." |

---

## 5. Implementation Guidelines

### 5.1 File Structure

```
app/src/main/java/com/calender28/
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   └── AppDatabase.kt          # Add CoinBalanceEntity & CoinTransactionEntity
│   │   ├── dao/
│   │   │   └── CoinDao.kt              # Complete CRUD operations
│   │   └── entities/
│   │       ├── CoinBalanceEntity.kt    # Single row table
│   │       └── CoinTransactionEntity.kt # Ledger
│   └── repository/
│       ├── CoinRepository.kt           # Business logic
│       └── StreakTracker.kt            # Streak validation
├── ui/
│   ├── viewmodel/
│   │   └── CoinViewModel.kt            # State management
│   └── screens/
│       └── store/
│           └── CoinStoreScreen.kt      # Full-screen store UI
└── utils/
    └── CoinAnimationHelper.kt          # Floating text animations
```

### 5.2 Integration Points

| Location | Action |
|----------|--------|
| **MainActivity** | Initialize CoinViewModel, observe coin balance |
| **HomeScreen** | Display coin balance in top bar |
| **HabitCompletionFlow** | Call `coinRepository.rewardHabitCompletion()` |
| **HabitProgressFlow** | Call `coinRepository.rewardPartialProgress()` |
| **TaskCheckOffFlow** | Call `coinRepository.rewardDailyTask()` and check streak |
| **AppLaunchFlow** | Call `coinRepository.rewardDailyLogin()` on first launch of day |
| **PremiumPurchaseFlow** | Call `coinRepository.buyPremiumWithCoins()` and grant entitlement |

### 5.3 Analytics Events to Track

| Event Name | Properties |
|------------|------------|
| `coin_earned` | `{ "amount": 10, "reason": "HABIT_CYCLE_COMPLETE", "new_balance": 45 }` |
| `coin_spent` | `{ "amount": 500, "reason": "PREMIUM_PURCHASE", "new_balance": 100 }` |
| `premium_unlocked_coins` | `{ "method": "coins", "coins_spent": 500 }` |
| `premium_unlocked_subscription` | `{ "method": "subscription" }` |
| `streak_achieved` | `{ "days": 7, "bonus": 50 }` |
| `milestone_reached` | `{ "milestone": "10_CYCLES", "bonus": 100 }` |
| `promo_code_used` | `{ "code": "SHIPATON2026", "bonus": 100 }` |

---

## 6. Testing Checklist

### 6.1 Manual Testing

| Test Case | Expected Result | Status |
|-----------|----------------|--------|
| Open app for first time | Balance = 0 | ⬜ |
| Open app second time same day | No +1 coin | ⬜ |
| Open app next day | +1 coin added | ⬜ |
| Complete habit cycle | +10 coins added | ⬜ |
| Log partial progress | +1 coin added per day | ⬜ |
| Complete recurring task | +2 coins added | ⬜ |
| Complete 7-day streak | +50 bonus added | ⬜ |
| Complete 14-day streak | +100 bonus added | ⬜ |
| Complete 30-day streak | +200 bonus added | ⬜ |
| Miss a day | Streak resets, no bonus | ⬜ |
| Reach 10 total cycles | +100 milestone bonus | ⬜ |
| Reach 50 total cycles | +500 milestone bonus | ⬜ |
| Reach 100 total cycles | +1000 milestone bonus | ⬜ |
| Purchase premium with 500+ coins | Premium unlocked, 500 deducted | ⬜ |
| Purchase premium with <500 coins | Error shown, no deduction | ⬜ |
| Enter valid promo code | Coins added | ⬜ |
| Enter invalid promo code | Error shown | ⬜ |
| Subscribe via RevenueCat | Premium unlocked | ⬜ |
| Clear app data | Coins reset to 0 | ⬜ |
| Reinstall app | Coins reset to 0 | ⬜ |

### 6.2 UI/UX Testing

| Test Case | Expected Result | Status |
|-----------|----------------|--------|
| Coin icon visible in top bar | Yes, left of sync button | ⬜ |
| Click coin icon | Opens full-screen store | ⬜ |
| Balance displays correctly | Shows current number | ⬜ |
| Transaction history scrolls | Shows recent transactions | ⬜ |
| Earn coin animation | Floating "+X" text + coin flip | ⬜ |
| Insufficient coins snackbar | Red warning message | ⬜ |
| Premium unlock celebration | Confetti or fireworks | ⬜ |
| Dark mode support | Colors adapt correctly | ⬜ |

---

## 7. Performance Considerations

| Aspect | Recommendation |
|--------|----------------|
| **Database Queries** | Use Flow for real-time updates; avoid blocking main thread |
| **Animations** | Use Compose animations; avoid heavy custom views |
| **Notifications** | Use Snackbar for in-app; optional system notification |
| **RevenueCat Calls** | Use coroutines; handle network failures gracefully |
| **Memory** | Transaction history limited to last 50 entries |

---

## 8. Security Considerations

| Risk | Mitigation |
|------|------------|
| **Coin Manipulation** | All coin logic in Repository; no direct database access from UI |
| **Promo Code Abuse** | One-time use per code; server-side validation recommended |
| **RevenueCat Spoofing** | Verify purchase state via RevenueCat SDK callbacks |
| **Local Storage Loss** | Inform users coins are local-only; no cloud backup |

---

## 9. Development Roadmap

| Phase | Tasks | Estimated Time |
|-------|-------|----------------|
| **Phase 1: Database** | Create entities, DAO, update AppDatabase | 1 hour |
| **Phase 2: Repository** | CoinRepository, StreakTracker, CoinViewModel | 2 hours |
| **Phase 3: UI** | CoinStoreScreen, top bar integration, animations | 2 hours |
| **Phase 4: Integration** | Wire into habit/task flows, RevenueCat | 2 hours |
| **Phase 5: Testing** | Manual testing, bug fixes | 2 hours |
| **Phase 6: Polish** | Animations, notifications, edge cases | 1 hour |
| **Total** | | **10 hours** |

---

## 10. Acceptance Criteria

- [ ] Users start with 0 CalCoins
- [ ] Users earn +1 coin on first app open of the day
- [ ] Users earn +10 coins per completed habit cycle
- [ ] Users earn +1 coin per day tracked in a habit cycle
- [ ] Users earn +2 coins per completed recurring task
- [ ] Users earn +50 bonus for 7-day streak
- [ ] Users earn +100 bonus for 14-day streak
- [ ] Users earn +200 bonus for 30-day streak
- [ ] Users earn milestone bonuses (10/50/100 cycles)
- [ ] Users can purchase premium for 500 coins
- [ ] Premium grants ad-free experience + all premium features
- [ ] RevenueCat is called to grant entitlement on coin purchase
- [ ] Existing RevenueCat subscription model still works
- [ ] Promo codes work
- [ ] Transaction history shows all earned/spent coins
- [ ] Coin balance displays on home screen top bar
- [ ] Clicking coin icon opens full-screen store
- [ ] Coins animate when earned
- [ ] Notifications shown on coin events
- [ ] All analytics events are tracked
- [ ] All edge cases handled gracefully
- [ ] No crashes or errors

---

## 11. Appendix

### A: Transaction Reason Codes

| Code | Description |
|------|-------------|
| `DAILY_LOGIN` | +1 for opening app daily |
| `HABIT_CYCLE_COMPLETE` | +10 for completing a habit cycle |
| `PARTIAL_HABIT_PROGRESS` | +1 per day logged in habit |
| `DAILY_TASK` | +2 per completed recurring task |
| `STREAK_7_DAY` | +50 bonus for 7-day streak |
| `STREAK_14_DAY` | +100 bonus for 14-day streak |
| `STREAK_30_DAY` | +200 bonus for 30-day streak |
| `MILESTONE_10_CYCLES` | +100 for 10 total cycles |
| `MILESTONE_50_CYCLES` | +500 for 50 total cycles |
| `MILESTONE_100_CYCLES` | +1000 for 100 total cycles |
| `PREMIUM_PURCHASE` | -500 for premium unlock |
| `PROMO_CODE` | Variable promo code bonus |

### B: Premium Features Comparison

| Feature | Free | Premium |
|---------|------|---------|
| Habit Limit | 5 | Unlimited |
| Ad Experience | With Ads | Ad-Free |
| Dark Mode | ❌ | ✅ |
| Advanced Stats | ❌ | ✅ |
| Data Export | ❌ | ✅ |
| Custom Themes | ❌ | ✅ |
| Priority Support | ❌ | ✅ |

### C: Promo Code Examples

| Code | Bonus | Expiry |
|------|-------|--------|
| `SHIPATON2026` | 100 Coins | Sep 30, 2026 |
| `WELCOME10` | 10 Coins | Permanent |
| `CALENDER28` | 50 Coins | Dec 31, 2026 |

---

