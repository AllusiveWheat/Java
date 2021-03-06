package src;

public class YahtzeeGame {
	private YahtzeeDice gameDice;
	private YahtzeeScoreCard gameScoreCard;

	public YahtzeeGame() {
		gameDice = new YahtzeeDice();
		gameScoreCard = new YahtzeeScoreCard();
	}

	public void startGame() {
		System.out.println("Hello, welcome to Yahtzee!");
		while (!gameOver()) {
			playTurn();
		}
	}

	public void playTurn() {
		boolean[] isHeldArray = new boolean[5];
		for (int b = 0; b < isHeldArray.length; b++) {
			isHeldArray[b] = false;
		}
		for (int i = 0; i < 3; i++) {
			gameDice.roll(isHeldArray);
			System.out.println(gameDice.toString());
			if (i < 2) {
				System.out.println("Do you want to roll again");
				boolean t = TextIO.getlnBoolean();
				if (t) {
					for (int j = 0; j < isHeldArray.length; j++) {
						System.out.println("Would you like to hold dice " + (j + 1));
						isHeldArray[j] = TextIO.getlnBoolean();
					}
				} else {
					i = 3;
				}
			}
		}
		System.out.println("Score Card \n" + gameScoreCard.printScoreCard());
		System.out.println("Your potential score is \n"
				+ gameScoreCard.printPotentialScoreCard(gameDice.getDiceValues()));

		System.out.println("What would you like to score this as?");
		int choice = TextIO.getlnInt();

		String choiceString = "";
		switch (choice) {
			case 1:
				choiceString = "Ones";
				break;
			case 2:
				choiceString = "Twos";
				break;
			case 3:
				choiceString = "Threes";
				break;
			case 4:
				choiceString = "Fours";
				break;
			case 5:
				choiceString = "Fives";
				break;
			case 6:
				choiceString = "Sixes";
				break;
			case 7:
				choiceString = "Three of a kind";
				break;
			case 8:
				choiceString = "Four of a kind";
				break;
			case 9:
				choiceString = "Full house";
				break;
			case 10:
				choiceString = "Small straight";
				break;
			case 11:
				choiceString = "Large straight";
				break;
			case 12:
				choiceString = "Chance";
				break;
			case 13:
				choiceString = "Yahtzee";
				break;
			default:
				System.out.println("Invalid choice");
				break;
		}
		if (gameScoreCard.isUsed(choice)) {
			System.out.println("You have already scored this category");
		} else {
			choice = choice - 1;
			gameScoreCard.setValue(choice, gameDice.getDiceValues());
			System.out.println("You scored " + choiceString);
			int roundScore = gameScoreCard.getValue(choice);
			System.out.println("Your round score is " + roundScore);

		}
		int score = gameScoreCard.totalScore();
		System.out.println("Your total score is " + score);

	}


	/**
	 * @return boolean
	 */
	public boolean gameOver() {
		return gameScoreCard.gameOver();
	}


}
