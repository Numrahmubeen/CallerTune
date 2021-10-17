package com.appsuite.prioritycontacts.models;

import android.graphics.Bitmap;

public class ContactModel
{
        private String id;
        private String name;
        private String mobileNumber;
        private boolean selected;
        private Bitmap photo;
        private String photoUri;
        private String callRingMode, msgRingMode;

        public ContactModel(String name, String mobileNumber, Bitmap photo, String msgRingMode) {
                this.name = name;
                this.mobileNumber = mobileNumber;
                this.photo = photo;
                this.msgRingMode = msgRingMode;
        }

        public String getCallRingMode() {
                return callRingMode;
        }

        public void setCallRingMode(String callRingMode) {
                this.callRingMode = callRingMode;
        }

        public String getMsgRingMode() {
                return msgRingMode;
        }

        public void setMsgRingMode(String msgRingMode) {
                this.msgRingMode = msgRingMode;
        }

        public String getPhotoUri() {
                return photoUri;
        }

        public void setPhotoUri(String photoUri) {
                this.photoUri = photoUri;
        }

        public Bitmap getPhoto() {
                return photo;
        }

        public void setPhoto(Bitmap photo) {
                this.photo = photo;
        }

        public ContactModel() {
                selected = false;
        }

        public boolean isSelected() {
                return selected;
        }

        public void setSelected(boolean selected) {
                this.selected = selected;
        }

        public String getId() {
                return id;
        }

        public void setId(String id) {
                this.id = id;
        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        public String getMobileNumber() {
                return mobileNumber;
        }

        public void setMobileNumber(String mobileNumber) {
                this.mobileNumber = mobileNumber;
        }

}
