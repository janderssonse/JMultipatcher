#!/usr/bin/env python

import subprocess
import gi
gi.require_version('Gtk', '3.0')
from gi.repository import Gtk

class ButtonHandler:

	# Save locations
	romSaveLocation = ""
	patchSaveLocation = ""

	# File paths
	unmodifiedRom = ""
	toBePatchedRom = ""
	modifiedRom = ""
	patchPath = ""

	# Consoles
	createCmdOutput = None
	applyCmdOutput = None

	def __init__(self, builder):
		# Init consoles
		view = builder.get_object("createCmdOutput")
		self.createCmdOutput = view.get_buffer()
		view = builder.get_object("applyCmdOutput")
		self.applyCmdOutput = view.get_buffer()

		# Init FileChoosers
		self.unmodifiedRom = builder.get_object("unModifiedChooser")
		self.toBePatchedRom = builder.get_object("romChooser")
		self.modifiedRom = builder.get_object("modifiedChooser")
		self.patchPath = builder.get_object("patchChooser")

	def onPatchSaveLocationButton(self, button):
		dialog = Gtk.FileChooserDialog(
			"Please choose a save location",
			None,
			Gtk.FileChooserAction.SAVE,
			(Gtk.STOCK_CANCEL, Gtk.ResponseType.CANCEL,
			Gtk.STOCK_SAVE, Gtk.ResponseType.OK)
			)

		response = dialog.run()

		if response == Gtk.ResponseType.OK:
			button.set_label(dialog.get_filename())
			self.patchSaveLocation = dialog.get_filename()
		elif response == Gtk.ResponseType.CANCEL:
			pass

		dialog.destroy()

	def onRomSaveLocationButton(self, button):
		dialog = Gtk.FileChooserDialog(
			"Please choose a save location",
			None,
			Gtk.FileChooserAction.SAVE,
			(Gtk.STOCK_CANCEL, Gtk.ResponseType.CANCEL,
			Gtk.STOCK_SAVE, Gtk.ResponseType.OK)
			)

		response = dialog.run()

		if response == Gtk.ResponseType.OK:
			button.set_label(dialog.get_filename())
			self.romSaveLocation = dialog.get_filename()
		elif response == Gtk.ResponseType.CANCEL:
			pass

		dialog.destroy()

	def onCreatePatch(self, button):
		if self.modifiedRom.get_filename() != None and self.unmodifiedRom.get_filename() != None and self.patchSaveLocation != "":
			self.createCmdOutput.insert_at_cursor("Creating patch...\n")
			self.createCmdOutput.insert_at_cursor(subprocess.check_output(["java", "-jar", "JMultiPatcher-1.0-SNAPSHOT.jar", "-s",
				self.unmodifiedRom.get_filename(), "-t", self.modifiedRom.get_filename(), "-p", self.patchSaveLocation,
				"-m", "Create"], universal_newlines=True))
		else:
			self.createCmdOutput.insert_at_cursor("Please set all options above\n")

	def onApplyPatch(self, button):
		if self.toBePatchedRom.get_filename() != None and self.patchPath.get_filename() != None and self.romSaveLocation != "":
			self.applyCmdOutput.insert_at_cursor("Applying patch\n")
			self.createCmdOutput.insert_at_cursor(subprocess.check_output(["java", "-jar", "JMultiPatcher-1.0-SNAPSHOT.jar", "-s",
				self.toBePatchedRom.get_filename(), "-t", self.romSaveLocation, "-p", self.patchPath.get_filename(),
				"-m", "Apply"], universal_newlines=True))
		else:
			self.applyCmdOutput.insert_at_cursor("Please set all options above\n")

builder = Gtk.Builder()
builder.add_from_file('ROMGui.glade')
buttonHandler = ButtonHandler(builder)
builder.connect_signals(buttonHandler)

window = builder.get_object("window1")
window.resize(800, 340)
window.show_all()

# Close application when the X is pressed
window.connect("delete-event", Gtk.main_quit)

Gtk.main()