package it.feio.android.omninotes.models;

public class Stats {
	
	int notesActive;
	int notesArchived;
	int notesTrashed;
	int reminders;
	int remindersFutures;
	int notesChecklist;
	int notesMasked;
	int categories;
	int tags;

	int attachments;
	int images;
	int videos;
	int audioRecordings;
	int sketches;
	int files;

	int words;
	int chars;
	long usageTime;
	

	public int getNotesTotalNumber() {
		return notesActive + notesArchived + notesTrashed;
	}

	public int getNotesActive() {
		return notesActive;
	}

	public void setNotesActive(int notesActive) {
		this.notesActive = notesActive;
	}

	public int getNotesArchived() {
		return notesArchived;
	}

	public void setNotesArchived(int notesArchived) {
		this.notesArchived = notesArchived;
	}

	public int getNotesTrashed() {
		return notesTrashed;
	}

	public void setNotesTrashed(int notesTrashed) {
		this.notesTrashed = notesTrashed;
	}

	public int getReminders() {
		return reminders;
	}

	public void setReminders(int reminders) {
		this.reminders = reminders;
	}

	public int getRemindersFutures() {
		return remindersFutures;
	}

	public void setRemindersFutures(int remindersFutures) {
		this.remindersFutures = remindersFutures;
	}

	public int getNotesChecklist() {
		return notesChecklist;
	}

	public void setNotesChecklist(int notesChecklist) {
		this.notesChecklist = notesChecklist;
	}

	public int getNotesMasked() {
		return notesMasked;
	}

	public void setNotesMasked(int notesMasked) {
		this.notesMasked = notesMasked;
	}

	public int getCategories() {
		return categories;
	}

	public void setCategories(int categories) {
		this.categories = categories;
	}

	public int getTags() {
		return tags;
	}

	public void setTags(int tags) {
		this.tags = tags;
	}

	public int getWords() {
		return words;
	}

	public void setWords(int words) {
		this.words = words;
	}

	public int getChars() {
		return chars;
	}

	public void setChars(int chars) {
		this.chars = chars;
	}

	public int getAttachments() {
		return attachments;
	}

	public void setAttachments(int attachments) {
		this.attachments = attachments;
	}

	public int getImages() {
		return images;
	}

	public void setImages(int images) {
		this.images = images;
	}

	public int getVideos() {
		return videos;
	}

	public void setVideos(int videos) {
		this.videos = videos;
	}

	public int getAudioRecordings() {
		return audioRecordings;
	}

	public void setAudioRecordings(int audioRecordings) {
		this.audioRecordings = audioRecordings;
	}

	public int getSketches() {
		return sketches;
	}

	public void setSketches(int sketches) {
		this.sketches = sketches;
	}

	public int getFiles() {
		return files;
	}

	public void setFiles(int files) {
		this.files = files;
	}

	public long getUsageTime() {
		return usageTime;
	}

	public void setUsageTime(long usageTime) {
		this.usageTime = usageTime;
	}
}
