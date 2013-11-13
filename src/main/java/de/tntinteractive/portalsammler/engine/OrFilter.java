/*
    Copyright (C) 2013  Tobias Baum <tbaum at tntinteractive.de>

    This file is a part of Portalsammler.

    Portalsammler is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Portalsammler is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Portalsammler.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tntinteractive.portalsammler.engine;

public class OrFilter extends DocumentFilter {

    private final DocumentFilter first;
    private final DocumentFilter second;

    public OrFilter(DocumentFilter first, DocumentFilter second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean shallShow(DocumentInfo s) {
        return this.first.shallShow(s) || this.second.shallShow(s);
    }

}
