import EmptyState from "@/components/common/EmptyState";
import ErrorAlert from "@/components/common/ErrorAlert";
import PageButton from "@/components/common/PageButton";
import PageHeader from "@/components/layout/PageHeader";
import TransactionsTable from "@/components/transactions/TransactionsTable";
import TransactionsTableSkeleton from "@/components/transactions/TransactionsTableSkeleton";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter } from "@/components/ui/card";
import useTransactions from "@/hooks/useTransactions";
import { parsePage } from "@/lib/pagination";
import { cn } from "cn";
import { Link, useSearchParams } from "react-router-dom";

const PAGE_SIZE = 10;

export default function TransactionsPage() {
  const [searchParams] = useSearchParams();
  const page = parsePage(searchParams);
  const { data, isPlaceholderData, isError, isPending, refetch, isFetching } =
    useTransactions({
      size: PAGE_SIZE,
      page: page - 1,
    });
  const shownPage = data ? data.page + 1 : page;

  return (
    <div className="flex flex-col gap-8">
      <PageHeader
        title="Transaction history"
        description="All your transfers and top-ups."
      />
      <Card className="[--card-spacing:--spacing(6)]">
        <CardContent
          className={cn(
            "transition-opacity",
            isPlaceholderData && "opacity-60",
          )}
          aria-busy={isPlaceholderData}
        >
          {isPending ? (
            <TransactionsTableSkeleton rows={PAGE_SIZE} />
          ) : isError && data === undefined ? (
            <ErrorAlert
              title="Couldn't load your history"
              description="The server didn't answer. Your money is safe. Check your connection and try again."
              isRetrying={isFetching}
              onRetry={() => refetch()}
            />
          ) : data.totalElements === 0 ? (
            <EmptyState
              title="No history yet"
              description="Your transactions will show up here."
            />
          ) : shownPage > data.totalPages ? (
            <EmptyState
              title="This page does not exist"
              description={`Your history has ${data.totalPages} ${data.totalPages === 1 ? "page" : "pages"}`}
            >
              <Button asChild variant="outline">
                <Link to={"?page=1"}>Go to page 1</Link>
              </Button>
            </EmptyState>
          ) : (
            <TransactionsTable transactions={data.content} />
          )}
        </CardContent>

        {data && data.totalPages > 1 && shownPage <= data.totalPages && (
          <CardFooter className="justify-between">
            <span className="caption">
              Page {shownPage} of {data.totalPages}
            </span>
            <div className="flex gap-2">
              <PageButton
                disabled={shownPage <= 1 || isPlaceholderData}
                to={page - 1}
              >
                Previous
              </PageButton>
              <PageButton
                disabled={shownPage >= data.totalPages || isPlaceholderData}
                to={page + 1}
              >
                Next
              </PageButton>
            </div>
          </CardFooter>
        )}
      </Card>
    </div>
  );
}
