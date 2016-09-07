/*
 * Jicofo, the Jitsi Conference Focus.
 *
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.impl.protocol.xmpp;

import net.java.sip.communicator.service.protocol.*;

import org.jivesoftware.smack.*;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Multi user chat implementation stripped to the minimum required by the focus
 * of Jitsi Meet conference. Uses Smack backend.
 *
 * @author Pawel Domas
 */
public class OperationSetMultiUserChatImpl
    extends AbstractOperationSetMultiUserChat
{
    /**
     * Parent protocol provider.
     */
    private final XmppProtocolProvider protocolProvider;

    /**
     * The map of active chat rooms mapped by their names.
     */
    private final Map<String, ChatRoomImpl> rooms
        = new HashMap<String, ChatRoomImpl>();

    private final ScheduledExecutorService offlineCleaner;

    /**
     * Creates new instance of {@link OperationSetMultiUserChatImpl}.
     *
     * @param protocolProvider parent protocol provider service.
     */
    OperationSetMultiUserChatImpl(XmppProtocolProvider protocolProvider)
    {
        this.protocolProvider = protocolProvider;

        this.offlineCleaner = Executors.newScheduledThreadPool(1);
        this.offlineCleaner.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (ChatRoomImpl room : new ArrayList<>(rooms.values())) {
                    room.cleanOfflineParticipants();
                }
            }
        }, 0, 2, TimeUnit.SECONDS);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getExistingChatRooms()
        throws OperationFailedException, OperationNotSupportedException
    {
        return new ArrayList<String>(rooms.keySet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChatRoom> getCurrentlyJoinedChatRooms()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getCurrentlyJoinedChatRooms(
        ChatRoomMember chatRoomMember)
        throws OperationFailedException, OperationNotSupportedException
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChatRoom createChatRoom(String roomName,
                                   Map<String, Object> roomProperties)
        throws OperationFailedException, OperationNotSupportedException
    {
        roomName = roomName.toLowerCase();

        if (rooms.containsKey(roomName))
        {
            throw new OperationFailedException(
                "Room '" + roomName + "' exists",
                OperationFailedException.GENERAL_ERROR);
        }

        ChatRoomImpl newRoom = new ChatRoomImpl(this, roomName);

        rooms.put(roomName, newRoom);

        return newRoom;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChatRoom findRoom(String roomName)
        throws OperationFailedException, OperationNotSupportedException
    {
        roomName = roomName.toLowerCase();

        ChatRoom room = rooms.get(roomName);
        if (room == null)
        {
            room = createChatRoom(roomName, null);
        }
        return room;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rejectInvitation(ChatRoomInvitation invitation,
                                 String rejectReason)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMultiChatSupportedByContact(Contact contact)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrivateMessagingContact(String contactAddress)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Returns Smack connection object used by parent protocol provider service.
     */
    public Connection getConnection()
    {
        return protocolProvider.getConnection();
    }

    /**
     * Returns parent protocol provider service.
     */
    public XmppProtocolProvider getProtocolProvider()
    {
        return protocolProvider;
    }

    public void removeRoom(ChatRoomImpl chatRoom)
    {
        rooms.remove(chatRoom.getName());
    }
}
