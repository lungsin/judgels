import { IconName } from '@blueprintjs/core';
import classNames from 'classnames';
import * as React from 'react';
import { Redirect, RouteComponentProps, Switch, withRouter } from 'react-router';

import { Sidebar, SidebarItem } from '../../components/Sidebar/Sidebar';

import './ContentWithSidebar.css';

export interface ContentAndSidebarProps {
  sidebarElement: JSX.Element;
  contentElement: JSX.Element;
  smallContent?: boolean;
}

const ContentAndSidebar = (props: ContentAndSidebarProps) => (
  <div className="content-with-sidebar">
    <div className="content-with-sidebar__sidebar">{props.sidebarElement}</div>
    <div
      className={classNames('content-with-sidebar__content', {
        'content-with-sidebar__content--small': props.smallContent,
      })}
    >
      {props.contentElement}
    </div>
  </div>
);

export interface ContentWithSidebarItem {
  id: string;
  titleIcon?: IconName;
  title: string | JSX.Element;
  routeComponent: any;
  component: any;
  disabled?: boolean;
}

export interface ContentWithSidebarProps {
  title: string;
  action?: JSX.Element;
  smallContent?: boolean;
  items: ContentWithSidebarItem[];
  contentHeader?: JSX.Element;
}

interface ContentWithSidebarConnectedProps extends RouteComponentProps<{}> {}

function resolveUrl(parentPath: string, childPath: string) {
  const actualChildPath = childPath === '@' ? '' : childPath;
  return (parentPath + '/' + actualChildPath).replace(/\/\/+/g, '/');
}

class ContentWithSidebar extends React.PureComponent<ContentWithSidebarProps & ContentWithSidebarConnectedProps> {
  render() {
    return (
      <ContentAndSidebar
        sidebarElement={this.renderSidebar()}
        contentElement={this.renderContent()}
        smallContent={this.props.smallContent}
      />
    );
  }

  private renderSidebar = () => {
    const sidebarItems = this.props.items.filter(item => !item.disabled).map(
      item =>
        ({
          id: item.id,
          titleIcon: item.titleIcon,
          title: item.title,
        } as SidebarItem)
    );

    return (
      <Sidebar
        title={this.props.title}
        action={this.props.action}
        activeItemId={this.getActiveItemId()}
        items={sidebarItems}
        onResolveItemUrl={this.onResolveItemUrl}
      />
    );
  };

  private renderContent = () => {
    const components = this.props.items.map(item => {
      const RouteC = item.routeComponent;
      const props = {
        exact: item.id === '@',
        path: resolveUrl(this.props.match.url, item.id),
        component: item.component,
      };
      return <RouteC key={item.id} {...props} />;
    });

    const redirect = this.props.items[0].id !== '@' && (
      <Redirect exact from={this.props.match.url} to={resolveUrl(this.props.match.url, this.props.items[0].id)} />
    );

    return (
      <div>
        {this.props.contentHeader}
        <Switch>
          {redirect}
          {components}
        </Switch>
      </div>
    );
  };

  private onResolveItemUrl = (itemId: string) => {
    return resolveUrl(this.props.match.url, itemId);
  };

  private getActiveItemId = () => {
    if (this.props.location.pathname === this.props.match.url) {
      return '@';
    }

    const currentPath = this.props.location.pathname + '/';
    const nextSlashPos = currentPath.indexOf('/', this.props.match.url.length + 1);
    return currentPath.substring(this.props.match.url.length + 1, nextSlashPos);
  };
}

export default withRouter<any, any>(ContentWithSidebar);
